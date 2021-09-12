package io.bottomfeeder.filter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Objects;

/**
 * Contains utility methods for working with date/time values related to filters.
 */
class DateTimeUtils {

	private DateTimeUtils() {}
	
	private static final DateTimeFormatter DATE_VALUE_PATTERN = new DateTimeFormatterBuilder()
			.appendPattern("dd.MM.yyyy")
			.parseDefaulting(ChronoField.NANO_OF_DAY, 0)
			.toFormatter()
			.withZone(ZoneId.of("Z"));
	
	private static final DateTimeFormatter DATE_TIME_VALUE_PATTERN = new DateTimeFormatterBuilder()
			.appendPattern("dd.MM.yyyy HH:mm:ss")
			.toFormatter()
			.withZone(ZoneId.of("Z"));
	
	
	static boolean isEqual(Instant instant1, Instant instant2) {
		return Objects.equals(instant1, instant2);
	}
	
	
	static boolean isBefore(Instant instant1, Instant instant2) {
		return instant1 != null && instant2 != null && instant1.isBefore(instant2);
	}
	
	
	static boolean isAfter(Instant instant1, Instant instant2) {
		return instant1 != null && instant2 != null && instant1.isAfter(instant2);
	}
	
	
	static Instant castAsInstant(Object dateTime) {
		if (dateTime == null)
			return null;
		else if (dateTime instanceof Instant instant)
			return instant;
		else if (dateTime instanceof Date date)
			return date.toInstant();
		else
			throw new IllegalArgumentException(String.format("Invalid date/time value: %s (class: %s)", 
					dateTime, dateTime.getClass()));
	}
	
	
	static Instant parseAsInstant(String dateTime) {
		var instant = tryParseInstant(dateTime, DATE_VALUE_PATTERN);
		if (instant == null)
			instant = tryParseInstant(dateTime, DATE_TIME_VALUE_PATTERN);
		if (instant == null)
			throw new IllegalArgumentException(String.format("Invalid date/time value: %s", dateTime));
		else
			return instant;
	}
	
	
	static boolean isValidDateTime(String dateTime) {
		return tryParseInstant(dateTime, DATE_VALUE_PATTERN) != null 
				|| tryParseInstant(dateTime, DATE_TIME_VALUE_PATTERN) != null;
	}
	
	
	private static Instant tryParseInstant(String dateTime, DateTimeFormatter format) {
		try {
			return format.parse(dateTime, Instant::from);
		}
		catch (DateTimeParseException e) {
			return null;
		}
	}
	
}
