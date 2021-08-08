package io.bottomfeeder.sourcefeed.entry;

import io.bottomfeeder.base.EntityException;

/**
 * Exception that describes an error occured during operation with source feed entry.
 */
@SuppressWarnings("serial")
class SourceFeedEntryException extends EntityException {
	
	SourceFeedEntryException(String message) {
		super(message);
	}

	SourceFeedEntryException(Throwable cause) {
		super(cause);
	}
	
}
