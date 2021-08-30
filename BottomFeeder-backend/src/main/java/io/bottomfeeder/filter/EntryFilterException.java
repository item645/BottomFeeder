package io.bottomfeeder.filter;

import io.bottomfeeder.base.EntityException;

/**
 * Exception that describes an error occured during operation with entry filter.
 */
@SuppressWarnings("serial")
class EntryFilterException extends EntityException {

	public EntryFilterException(String message) {
		super(message);
	}

}
