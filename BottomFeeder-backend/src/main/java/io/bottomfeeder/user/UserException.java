package io.bottomfeeder.user;

import io.bottomfeeder.base.EntityException;

/**
 * Exception that describes an error occured during operation with user
 */
@SuppressWarnings("serial")
class UserException extends EntityException {

	UserException(String message) {
		super(message);
	}
	
}
