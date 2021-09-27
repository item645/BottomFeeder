package io.bottomfeeder.data;

import static io.bottomfeeder.sourcefeed.SourceFeed.*;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.bottomfeeder.filter.model.SourceFeedEntryFilterData;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Container for imported or exported data representing a single source feed with its 
 * associated entry filters.
 */
record SourceFeedData(
		
		@NotNull(message = VALIDATION_SOURCE_NULL)
		@Size(message = VALIDATION_SOURCE_SIZE, min = SOURCE_MIN_SIZE, max = SOURCE_MAX_SIZE)
		String source,
		
		@Min(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MIN, value = CONTENT_UPDATE_INTERVAL_MIN)
		@Max(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MAX, value = CONTENT_UPDATE_INTERVAL_MAX)
		int contentUpdateInterval,
		
		@Min(message = VALIDATION_MAX_ENTRIES_MIN, value = MAX_ENTRIES_MIN)
		@Max(message = VALIDATION_MAX_ENTRIES_MAX, value = MAX_ENTRIES_MAX)
		int maxEntries,
		
		@NotNull(message = "{validation.source-feed-data.entry-filters.null}")
		List<SourceFeedEntryFilterData> entryFilters) {
	
	SourceFeedData {}
	
	SourceFeedData(SourceFeed sourceFeed, List<SourceFeedEntryFilterData> entryFilters) {
		this(
			sourceFeed.getSource(), 
			sourceFeed.getContentUpdateInterval(), 
			sourceFeed.getMaxEntries(),
			Objects.requireNonNull(entryFilters)
			);
	}
}
