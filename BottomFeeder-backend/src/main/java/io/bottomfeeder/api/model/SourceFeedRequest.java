package io.bottomfeeder.api.model;

import static io.bottomfeeder.sourcefeed.SourceFeed.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Contains request data for creating new source feed or updating existing one.
 */
public record SourceFeedRequest(
		
		Long id,
		
		@Positive
		long digestId,
		
		@NotNull(message = VALIDATION_SOURCE_NULL)
		@Size(message = VALIDATION_SOURCE_SIZE, min = SOURCE_MIN_SIZE, max = SOURCE_MAX_SIZE)
		String source,
		
		@Min(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MIN, value = CONTENT_UPDATE_INTERVAL_MIN)
		@Max(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MAX, value = CONTENT_UPDATE_INTERVAL_MAX)
		int contentUpdateInterval,
		
		boolean updateContent)

implements EntityModel<SourceFeed> {
	
	@Override
	public Long entityId() {
		return id();
	}

	@Override
	public Class<SourceFeed> entityClass() {
		return SourceFeed.class;
	}
}
