package io.bottomfeeder.filter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Represents entry filter for source feed.
 */
@Entity
@Table(name = "source_feed_entry_filter")
public class SourceFeedEntryFilter extends EntryFilter<SourceFeed> {

	private static final String VALIDATION_SOURCE_FEED_NULL = "{validation.entry-filter.source-feed.null}";

	@NotNull(message = VALIDATION_SOURCE_FEED_NULL)
	@ManyToOne(optional = false)
	@JoinColumn(name = "source_feed_id")
	private SourceFeed associatedEntity;
	
	public SourceFeedEntryFilter() {}
	
	
	@Override
	public SourceFeed getAssociatedEntity() {
		return associatedEntity;
	}

	@Override
	public void setAssociatedEntity(SourceFeed associatedEntity) {
		this.associatedEntity = associatedEntity;
	}
	
}
