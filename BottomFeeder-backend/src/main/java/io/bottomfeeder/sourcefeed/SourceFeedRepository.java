package io.bottomfeeder.sourcefeed;

import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import io.bottomfeeder.digest.Digest;

/**
 * Spring Data repository for source feeds.
 */
public interface SourceFeedRepository extends JpaRepository<SourceFeed, Long> {
	
	@Transactional(readOnly = false) // make Postgres happy
	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select sourceFeed from SourceFeed sourceFeed where sourceFeed.id = :id")
	Optional<SourceFeed> findAndLockById(long id);

	
	List<SourceFeed> findByDigest(Digest digest);
	
	
	List<SourceFeed> findByDigestOrderByCreationDateDesc(Digest digest);
	
	
	@Query("select sourceFeed.id from SourceFeed sourceFeed where sourceFeed.digest = :digest")
	List<Long> findIdsByDigest(Digest digest);
	
	
	boolean existsBySourceAndDigest(String source, Digest digest);
	
	
	@Query("""
			select 
				case when (count(sourceFeed) = 1) then true else false end 
			from 
				SourceFeed sourceFeed 
			where 
				sourceFeed.id = :sourceFeedId and :userId = sourceFeed.digest.owner.id
			""")
	boolean isSourceFeedDigestOwner(long sourceFeedId, Long userId);
	
}
