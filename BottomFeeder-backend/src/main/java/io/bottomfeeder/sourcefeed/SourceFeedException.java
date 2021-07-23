package io.bottomfeeder.sourcefeed;

import io.bottomfeeder.base.EntityException;

/**
 * Exception that describes an error occured during operation with source feed.
 */
@SuppressWarnings("serial")
public class SourceFeedException extends EntityException {

	public SourceFeedException(String message) {
		super(message);
	}

	public SourceFeedException(Throwable cause) {
		super(cause);
	}

	public SourceFeedException(String message, Throwable cause) {
		super(message, cause);
	}

}
