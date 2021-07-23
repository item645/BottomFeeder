package io.bottomfeeder.sourcefeed;

import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import io.bottomfeeder.digest.Digest;

/**
 * Spring Data repository for source feeds.
 * 
 * The "summary" methods return partially constructed source feed entities with all fields 
 * initialized except content field.
 * These methods should be preferred over their regular counterparts (that return fully constructed
 * entities) in situations where the value of content field is never read or otherwise used, because
 * they don't have associated performance penalty caused by the loading of heavyweight content
 * data from the DB.
 */
public interface SourceFeedRepository extends JpaRepository<SourceFeed, Long> {

	static final String SUMMARY_CONSTRUCTOR = """
			new io.bottomfeeder.sourcefeed.SourceFeed(
					sourceFeed.id, 
					sourceFeed.source, 
					sourceFeed.title, 
					sourceFeed.creationDate, 
					sourceFeed.contentUpdateDate, 
					sourceFeed.contentUpdateInterval, 
					sourceFeed.digest)
			""";

	
	@Query("select " + SUMMARY_CONSTRUCTOR + " from SourceFeed sourceFeed where sourceFeed.id = :id")
	Optional<SourceFeed> findSummaryById(long id);

	
	@Transactional(readOnly = false) // make Postgres happy
	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select sourceFeed from SourceFeed sourceFeed where sourceFeed.id = :id")
	Optional<SourceFeed> findAndLockById(long id);
	
	
	@Transactional(readOnly = false)
	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select " + SUMMARY_CONSTRUCTOR + " from SourceFeed sourceFeed where sourceFeed.id = :id")
	Optional<SourceFeed> findSummaryAndLockById(long id);
	
	
	@Query("select " + SUMMARY_CONSTRUCTOR + " from SourceFeed sourceFeed")
	List<SourceFeed> findAllSummaries();

	
	@Query("select sourceFeed.id from SourceFeed sourceFeed where sourceFeed.digest = :digest")
	List<Long> findIdsByDigest(Digest digest);
	
	
	@Query("""
			select 
				""" + SUMMARY_CONSTRUCTOR + """ 
			from 
				SourceFeed sourceFeed 
			where
				sourceFeed.digest = :digest 
			order by
				sourceFeed.creationDate desc
			""")
	List<SourceFeed> findSummariesByDigest(Digest digest);
	
	
	List<SourceFeed> findByDigest(Digest digest);
	
	
	boolean existsBySourceAndDigest(String source, Digest digest);
	
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete SourceFeed sourceFeed where sourceFeed.digest = :digest")
	int deleteByDigest(Digest digest);
	
	
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
