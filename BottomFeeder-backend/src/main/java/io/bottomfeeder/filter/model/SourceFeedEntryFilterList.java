package io.bottomfeeder.filter.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * A container for the list of source feed entry filters.
 */
public record SourceFeedEntryFilterList(
		
		@Valid
		@NotNull(message = "{validation.entry-filter-list.source-feed-filters.null}")
		List<SourceFeedEntryFilterData> filters) 

implements EntryFilterList<SourceFeedEntryFilter, SourceFeed>{
}
