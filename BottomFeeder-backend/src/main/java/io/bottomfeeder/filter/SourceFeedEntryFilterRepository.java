package io.bottomfeeder.filter;

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Spring Data repository for source feed entry filters.
 */
public interface SourceFeedEntryFilterRepository extends EntryFilterRepository<SourceFeedEntryFilter, SourceFeed> {
}
