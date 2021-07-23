package io.bottomfeeder.security.auth;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;

import io.bottomfeeder.api.error.APIExceptionHandler;

/**
 * Base class for default AuthenticationEntryPoint and AccessDeniedHandler implementations.
 * 
 * @param <E> the type of exception handled by this handler
 */
abstract class HandlerBase<E extends RuntimeException> {

	private final ApplicationContext applicationContext;
	protected final HandlerExceptionResolver handlerExceptionResolver;
	
	
	protected HandlerBase(ApplicationContext applicationContext, HandlerExceptionResolver handlerExceptionResolver) {
		this.applicationContext = applicationContext;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	
	protected HandlerMethod getHandlerMethod(String methodName, Class<E> exceptionType) {
		// Kind of a hack to explicitly specify a handler method for handling auth-based exceptions
		// thrown within filter chain and processed by ExceptionTranslationFilter.
		// This is necessary because ExceptionHandlerExceptionResolver cannot properly delegate exception
		// handling to @RestControllerAdvice-annotated class when that class is restricted to base packages.
		try {
			return new HandlerMethod(applicationContext.getBean(APIExceptionHandler.class),
					methodName, exceptionType);
		}
		catch (BeansException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
}
