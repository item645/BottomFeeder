package io.bottomfeeder.base;

/**
 * Base class for exceptions related to application entities.
 */
@SuppressWarnings("serial")
public abstract class EntityException extends RuntimeException {

	public EntityException(String message) {
		super(message);
	}

	public EntityException(Throwable cause) {
		super(cause);
	}

	public EntityException(String message, Throwable cause) {
		super(message, cause);
	}

}
