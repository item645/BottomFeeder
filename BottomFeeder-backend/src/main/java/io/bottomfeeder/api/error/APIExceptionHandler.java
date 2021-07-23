package io.bottomfeeder.api.error;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.bottomfeeder.api.model.ErrorResponse;
import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.base.EntityException;

/**
 * Provides centralized exception handling for all REST API controllers.
 */
@RestControllerAdvice(basePackages = "io.bottomfeeder.api")
public class APIExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(APIExceptionHandler.class);
	
	private static final HttpHeaders EMPTY_HEADERS = new HttpHeaders();
	

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleError(Exception exception) {
		logger.error("Server error", exception);
		return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, EMPTY_HEADERS, 
				"Server error (refer to server log for details)");
	} 

	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
			HttpStatus httpStatus, WebRequest request) {
		super.handleExceptionInternal(exception, body, headers, httpStatus, request);
		return createResponse(httpStatus, headers, "Server error", exception);
	}

	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException exception, 
			HttpHeaders headers, HttpStatus httpStatus, WebRequest request) {
		super.handleNoHandlerFoundException(exception, headers, httpStatus, request);
		return createResponse(httpStatus, headers, "Requested resource not found", exception);
	}


	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Object> handleAuthenticationError(AuthenticationException exception) {
		return createResponse(HttpStatus.UNAUTHORIZED, EMPTY_HEADERS, "Authentication error", exception);
	} 
	
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDeniedError(AccessDeniedException exception) {
		// TODO find a way to localize exception message (for some reason it doesn't work with messages.properties)
		return createResponse(HttpStatus.FORBIDDEN, EMPTY_HEADERS, "Access denied", 
				List.of("Access to this resource is denied"));
	}
	
	
	@ExceptionHandler(EntityException.class)
	public ResponseEntity<Object> handleEntityError(EntityException exception) {
		logger.debug("Entity error", exception);
		return createResponse(HttpStatus.BAD_REQUEST, EMPTY_HEADERS, exception.getMessage());
	}
	
	
	@ExceptionHandler(TransactionSystemException.class)
	public ResponseEntity<Object> handleTransactionSystemError(TransactionSystemException exception) {
		var rootCause = exception.getRootCause();
		if (rootCause instanceof ConstraintViolationException cve)
			return handleConstraintViolation(cve);
		else
			return handleError(exception);
	}
	
	
	@ExceptionHandler(ConcurrencyFailureException.class)
	public ResponseEntity<Object> handleDaoConcurrencyFailure(ConcurrencyFailureException exception) {
		logger.error("Data concurrency error", exception);
		return createResponse(HttpStatus.CONFLICT, EMPTY_HEADERS, 
				"Operation cannot be performed due to an ongoing concurrent update on same data");
	}
	
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException exception) {
		var details = exception.getConstraintViolations().stream()
				.map(APIExceptionHandler::createConstraintViolationMessage)
				.collect(Collectors.toList());
		return createResponse(HttpStatus.BAD_REQUEST, EMPTY_HEADERS, "Validation error", details);
	}
	
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		var details = exception.getBindingResult().getAllErrors().stream()
				.map(APIExceptionHandler::createObjectErrorMessage)
				.collect(Collectors.toList());
		return createResponse(HttpStatus.BAD_REQUEST, headers, "Validation error", details);
	}
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		var cause = exception.getCause();
		if (cause instanceof InvalidFormatException ife)
			return createInvalidFormatResponse(ife);
		else
			return createResponse(HttpStatus.BAD_REQUEST, headers, "Invalid request data", exception);
	}

	
	private static ResponseEntity<Object> createResponse(HttpStatus httpStatus, HttpHeaders httpHeaders,
			String description) {
		return createResponse(httpStatus, httpHeaders, description, List.of());
	}
	
	
	private static ResponseEntity<Object> createResponse(HttpStatus httpStatus, HttpHeaders httpHeaders, 
			String description, Throwable exception) {
		return createResponse(httpStatus, httpHeaders, description, List.of(exception.getMessage()));
	}
	
	
	@SuppressWarnings("unchecked")
	private static ResponseEntity<Object> createResponse(HttpStatus httpStatus, HttpHeaders httpHeaders, 
			String description, Collection<String> details) {
		var message = String.format("Error: %s", httpStatus.getReasonPhrase());
		var response = new Response<>(httpStatus.value(), message, new ErrorResponse(description, details));
		return (ResponseEntity<Object>)(ResponseEntity<?>)response.toResponseEntity(httpHeaders);
	}
	
	
	private static ResponseEntity<Object> createInvalidFormatResponse(InvalidFormatException exception) {
		var status = HttpStatus.BAD_REQUEST;
		var description = "Invalid request data format";
		var targetType = exception.getTargetType();
		
		if (targetType != null && Enum.class.isAssignableFrom(targetType)) {
			var path = exception.getPath();
			var fieldName = path.isEmpty() ? "(unknown)" : path.get(0).getFieldName();
			var detail = String.format("Invalid value '%s' of property '%s'. Permitted values: %s",
					exception.getValue(), fieldName, Arrays.toString(targetType.getEnumConstants()));
			return createResponse(status, EMPTY_HEADERS, description, List.of(detail));
		}
		else {
			return createResponse(status, EMPTY_HEADERS, description, exception);
		}		
	}
	
	
	private static String createPropertyErrorMessage(Object value, String property, String message) {
		if (property.equals("password") || property.equals("newPassword") || property.equals("currentPassword"))
			return String.format("Invalid value of property '%s': %s", property, message);
		else
			return String.format("Invalid value '%s' of property '%s'. %s", value, property, message);
	}
	
	
	private static String createConstraintViolationMessage(ConstraintViolation<?> violation) {
		return createPropertyErrorMessage(violation.getInvalidValue(), 
				violation.getPropertyPath().toString(), violation.getMessage());
	}

	
	private static String createObjectErrorMessage(ObjectError error) {
		if (error instanceof FieldError fieldError) {
			return createPropertyErrorMessage(fieldError.getRejectedValue(), 
					fieldError.getField(), fieldError.getDefaultMessage());
		}
		else {
			return String.format("Error in object '%s': %s", error.getObjectName(), error.getDefaultMessage());
		}
	}
	
}
