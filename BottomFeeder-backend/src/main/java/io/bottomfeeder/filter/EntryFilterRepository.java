package io.bottomfeeder.filter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base interface for entry filter Spring Data repositories.
 * 
 * @param <T> the type of entry filter entity
 * @param <E> the type of entity that filter entity is associated with
 */
@NoRepositoryBean
interface EntryFilterRepository<T extends EntryFilter<E>, E> extends JpaRepository<T, Long> {

	List<T> findByAssociatedEntity(E associatedEntity);
	
	
	List<T> findByAssociatedEntityOrderByOrdinal(E associatedEntity);
	
	
	default Map<Long, T> findByAssociatedEntityAndMapById(E associatedEntity) {
		return findByAssociatedEntity(associatedEntity).stream()
				.collect(Collectors.toMap(EntryFilter::getId, Function.identity()));
	}
	
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete #{#entityName} entity where entity.associatedEntity.id = :associatedEntityId")
	int deleteByAssociatedEntityId(long associatedEntityId);
}
