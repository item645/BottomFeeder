package io.bottomfeeder.api.model;

import java.time.Instant;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Contains response data for source feed.
 */
public record SourceFeedResponse(
		long id,
		long digestId,
		String source,
		String title,
		Instant creationDate,
		Instant contentUpdateDate,
		int contentUpdateInterval)

implements EntityModel<SourceFeed> {
	
	public SourceFeedResponse(SourceFeed sourceFeed) {
		this(
			sourceFeed.getId(),
			sourceFeed.getDigest().getId(),
			sourceFeed.getSource(),
			sourceFeed.getTitle(),
			sourceFeed.getCreationDate(),
			sourceFeed.getContentUpdateDate(),
			sourceFeed.getContentUpdateInterval()
			);
	}

	@Override
	public Long entityId() {
		return id();
	}

	@Override
	public Class<SourceFeed> entityClass() {
		return SourceFeed.class;
	}
}
