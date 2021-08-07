package io.bottomfeeder.sourcefeed.entry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Spring Data repository for source feed entries.
 */
interface SourceFeedEntryRepository extends JpaRepository<SourceFeedEntry, Long> {

	List<SourceFeedEntry> findBySourceFeed(SourceFeed sourceFeed);
	
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete SourceFeedEntry sourceFeedEntry where sourceFeedEntry.sourceFeed.id = :sourceFeedId")
	int deleteBySourceFeedId(long sourceFeedId);
	
}
