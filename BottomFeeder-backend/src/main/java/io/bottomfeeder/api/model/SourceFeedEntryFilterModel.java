package io.bottomfeeder.api.model;

import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Contains request or response data for individual source feed entry filter.
 */
public final class SourceFeedEntryFilterModel extends AbstractEntryFilterModel<SourceFeedEntryFilter, SourceFeed> {

	public SourceFeedEntryFilterModel(Long id, int ordinal, String element, Condition condition, 
			String value, Connective connective) {
		super(id, ordinal, element, condition, value, connective);
	}

	public SourceFeedEntryFilterModel(SourceFeedEntryFilter entryFilter) {
		super(entryFilter);
	}

	@Override
	public Class<SourceFeedEntryFilter> entityClass() {
		return SourceFeedEntryFilter.class;
	}

}
