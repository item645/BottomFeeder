package io.bottomfeeder.digest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.bottomfeeder.user.User;

/**
 * Spring Data repository for digests.
 */
public interface DigestRepository extends JpaRepository<Digest, Long> {

	Optional<Digest> findOneByExternalId(String externalId);
	
	
	List<Digest> findByOwner(User owner);
	
	
	List<Digest> findByOwnerOrderByCreationDateDesc(User owner);
	
	
	boolean existsByExternalId(String externalId);
	
	
	boolean existsByTitleIgnoreCaseAndOwner(String title, User owner);
	
	
	@Query("""
			select 
				case when (count(digest) = 1) then true else false end 
			from 
				Digest digest 
			where 
				digest.id = :digestId and :userId = digest.owner.id
		   """)
	boolean isDigestOwner(long digestId, Long userId);
	
	
	@Query("""
			select 
				case when (count(digest) = 1) then true else false end 
			from 
				Digest digest 
			where 
				digest.externalId = :digestExternalId and 
				(:userId = digest.owner.id or
				digest.isPrivate = false)
				
		   """)
	boolean canAccessDigestFeed(String digestExternalId, Long userId);
}
