package io.bottomfeeder.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Contains shared utility methods for REST API controllers. 
 */
class Utils {

	private Utils() {}
	
	
	static ResponseEntity<byte[]> createBinaryJsonResponse(byte[] content) {
		return ResponseEntity.ok()
				.contentLength(content.length)
				.contentType(MediaType.APPLICATION_JSON)
				.body(content);
	}

}
