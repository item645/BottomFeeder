package io.bottomfeeder.filter;

import org.springframework.data.jpa.repository.Query;

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Spring Data repository for source feed entry filters.
 */
public interface SourceFeedEntryFilterRepository extends EntryFilterRepository<SourceFeedEntryFilter, SourceFeed> {
	
	@Query("""
			select 
				case when (count(sourceFeedEntryFilter) = 1) then true else false end 
			from 
				SourceFeedEntryFilter sourceFeedEntryFilter 
			where 
				sourceFeedEntryFilter.id = :entryFilterId and 
				:userId = sourceFeedEntryFilter.associatedEntity.digest.owner.id
		   """)
	@Override
	boolean isAssociatedEntityOwner(long entryFilterId, Long userId);
	
}
