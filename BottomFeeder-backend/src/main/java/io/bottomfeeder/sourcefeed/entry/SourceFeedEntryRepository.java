package io.bottomfeeder.sourcefeed.entry;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Spring Data repository for source feed entries.
 */
interface SourceFeedEntryRepository extends JpaRepository<SourceFeedEntry, Long> {

	List<SourceFeedEntry> findBySourceFeed(SourceFeed sourceFeed);
	
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete SourceFeedEntry sourceFeedEntry where sourceFeedEntry.sourceFeed.id = :sourceFeedId")
	int deleteBySourceFeedId(long sourceFeedId);
	
	
	@Query("""
			select 
				sourceFeedEntry
			from 
				SourceFeedEntry sourceFeedEntry 
			where 
				sourceFeedEntry.sourceFeed.digest = :digest 
			order by 
				sourceFeedEntry.date desc
			""")
	List<SourceFeedEntry> findDigestFeedEntries(Digest digest, Pageable limit);
	
	
	default List<SourceFeedEntry> findDigestFeedEntries(Digest digest) {
		return findDigestFeedEntries(digest, PageRequest.of(0, digest.getMaxItems()));
	}
	
}
