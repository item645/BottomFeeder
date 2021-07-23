package io.bottomfeeder.api.model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A response encapsulating information related to the error occured during the attempt
 * to access REST API resource.
 * Provides general description of the error and optional detailed information. 
 */
public record ErrorResponse(String description, Collection<String> details) {
	
	public ErrorResponse {
		Objects.requireNonNull(details, "Details collection cannot be null");
	}
	
	public ErrorResponse(String description, String detail) {
		this(description, List.of(detail));
	}
	
}
