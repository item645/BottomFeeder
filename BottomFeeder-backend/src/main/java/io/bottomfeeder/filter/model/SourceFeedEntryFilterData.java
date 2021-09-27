package io.bottomfeeder.filter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.Element;
import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Contains data for individual source feed entry filter.
 */
public final class SourceFeedEntryFilterData extends AbstractEntryFilterData<SourceFeedEntryFilter, SourceFeed> {
	
	@JsonCreator
	public SourceFeedEntryFilterData(
			@JsonProperty("id") Long id,
			@JsonProperty("ordinal") int ordinal,
			@JsonProperty("element") Element element,
			@JsonProperty("condition") Condition condition,
			@JsonProperty("value") String value,
			@JsonProperty("connective") Connective connective) {
		super(id, ordinal, element, condition, value, connective);
	}

	public SourceFeedEntryFilterData(SourceFeedEntryFilter entryFilter) {
		super(entryFilter);
	}

	@Override
	public Class<SourceFeedEntryFilter> entityClass() {
		return SourceFeedEntryFilter.class;
	}

}
