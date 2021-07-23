package io.bottomfeeder.api.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * A generic container for JSON-convertible response returned by REST API controllers.
 * Contains HTTP status code, optional message and actual response data returned by 
 * REST resource, which can be a single object or a collection.
 * 
 * @param <T> the type of response data
 */
public record Response<T>(int httpStatus, String message, T data) {
	
	private static final int DEFAULT_HTTP_STATUS = HttpStatus.OK.value();
	
	public Response {}
	
	public Response() {
		this(DEFAULT_HTTP_STATUS, null, null);
	}
	
	public Response(String message) {
		this(DEFAULT_HTTP_STATUS, message, null);
	}
	
	public Response(T data) {
		this(DEFAULT_HTTP_STATUS, null, data);
	}
	
	public Response(String message, T data) {
		this(DEFAULT_HTTP_STATUS, message, data);
	}
	
	public Response(int httpStatus, T data) {
		this(httpStatus, null, data);
	}
	
	public ResponseEntity<Response<T>> toResponseEntity(HttpHeaders headers) {
		return ResponseEntity.status(httpStatus).headers(headers).body(this);
	}
	
}
