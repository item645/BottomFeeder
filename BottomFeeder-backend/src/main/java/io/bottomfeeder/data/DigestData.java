package io.bottomfeeder.data;

import static io.bottomfeeder.digest.Digest.*;

import java.util.Collection;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.bottomfeeder.digest.Digest;

/**
 * Container for imported or exported data representing a single digest together
 * with its associated source feeds.
 */
record DigestData(
		
		@NotNull(message = VALIDATION_EXTERNAL_ID_NULL)
		@Pattern(message = VALIDATION_EXTERNAL_ID_REGEX, regexp = EXTERNAL_ID_REGEX)
		@Size(message = VALIDATION_EXTERNAL_ID_SIZE, min = EXTERNAL_ID_SIZE, max = EXTERNAL_ID_SIZE)
		String externalId,
		
		@NotNull(message = VALIDATION_TITLE_NULL)
		@Size(message = VALIDATION_TITLE_SIZE, min = TITLE_MIN_SIZE, max = TITLE_MAX_SIZE)
		String title,
		
		@Min(message = VALIDATION_MAX_ENTRIES_MIN, value = MAX_ENTRIES_MIN)
		@Max(message = VALIDATION_MAX_ENTRIES_MAX, value = MAX_ENTRIES_MAX)
		int maxEntries,
		
		boolean isPrivate,
		
		@NotNull(message = "{validation.digest-data.source-feeds.null}")
		Collection<SourceFeedData> sourceFeeds) {
	
	DigestData {}
	
	DigestData(Digest digest, Collection<SourceFeedData> sourceFeeds) {
		this(
			digest.getExternalId(),
			digest.getTitle(),
			digest.getMaxEntries(),
			digest.isPrivate(),
			Objects.requireNonNull(sourceFeeds)
			);
	}
}
