package io.bottomfeeder.filter;

import org.springframework.data.jpa.repository.Query;

import io.bottomfeeder.digest.Digest;

/**
 * Spring Data repository for digest entry filters.
 */
public interface DigestEntryFilterRepository extends EntryFilterRepository<DigestEntryFilter, Digest> {
	
	@Query("""
			select 
				case when (count(digestEntryFilter) = 1) then true else false end 
			from 
				DigestEntryFilter digestEntryFilter 
			where 
				digestEntryFilter.id = :entryFilterId and :userId = digestEntryFilter.associatedEntity.owner.id
		   """)
	@Override
	boolean isAssociatedEntityOwner(long entryFilterId, Long userId);
	
}
