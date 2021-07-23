package io.bottomfeeder.digest;

import io.bottomfeeder.base.EntityException;

/**
 * Exception that describes an error occured during operation with digest.
 */
@SuppressWarnings("serial")
class DigestException extends EntityException {

	DigestException(String message) {
		super(message);
	}

}
