package io.bottomfeeder.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.filter.EntryFilterList;
import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Contains request or response data for the list of source feed entry filters.
 */
public record SourceFeedEntryFilterListModel(
		
		@Valid
		@NotNull(message = "{validation.entry-filter-list.source-feed-filters.null}")
		List<SourceFeedEntryFilterModel> filters) 

implements EntryFilterList<SourceFeedEntryFilter, SourceFeed>{
}
