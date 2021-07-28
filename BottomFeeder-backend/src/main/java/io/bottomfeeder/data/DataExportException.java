package io.bottomfeeder.data;

/**
 * Exception thrown when there is an error during data export.
 */
@SuppressWarnings("serial")
public class DataExportException extends RuntimeException {

	public DataExportException(Throwable cause) {
		super(cause);
	}
	
}
