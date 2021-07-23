package io.bottomfeeder.data;

import static io.bottomfeeder.sourcefeed.SourceFeed.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Container for imported or exported data representing a single source feed.
 */
record SourceFeedData(
		
		@NotNull(message = VALIDATION_SOURCE_NULL)
		@Size(message = VALIDATION_SOURCE_SIZE, min = SOURCE_MIN_SIZE, max = SOURCE_MAX_SIZE)
		String source,
		
		@Min(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MIN, value = CONTENT_UPDATE_INTERVAL_MIN)
		@Max(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MAX, value = CONTENT_UPDATE_INTERVAL_MAX)
		int contentUpdateInterval) {
}
