package io.bottomfeeder.data;

/**
 * Exception thrown when there is an error during data import.
 */
@SuppressWarnings("serial")
public class DataImportException extends RuntimeException {
	
	public DataImportException(String message) {
		super(message);
	}

	public DataImportException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
