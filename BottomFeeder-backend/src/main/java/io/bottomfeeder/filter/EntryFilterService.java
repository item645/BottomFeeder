package io.bottomfeeder.filter;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * A service providing common functionality for working with entry filters.
 */
@Service
public class EntryFilterService {

	private final DigestEntryFilterRepository digestEntryFilterRepository;
	private final SourceFeedEntryFilterRepository sourceFeedEntryFilterRepository;
	
	
	public EntryFilterService(
			DigestEntryFilterRepository digestEntryFilterRepository,
			SourceFeedEntryFilterRepository sourceFeedEntryFilterRepository) {
		this.digestEntryFilterRepository = digestEntryFilterRepository;
		this.sourceFeedEntryFilterRepository = sourceFeedEntryFilterRepository;
	}
	
	
	public List<DigestEntryFilter> getDigestEntryFilters(Digest digest) {
		return digestEntryFilterRepository.findByAssociatedEntityOrderByOrdinal(digest);
	}
	
	
	@Transactional
	public List<DigestEntryFilter> updateDigestEntryFilters(
			EntryFilterList<DigestEntryFilter, Digest> filterList, Digest digest) {
		return processFilterList(filterList, digestEntryFilterRepository, Objects.requireNonNull(digest), 
				DigestEntryFilter::new);
	}

	
	public List<SourceFeedEntryFilter> getSourceFeedEntryFilters(SourceFeed sourceFeed) {
		return sourceFeedEntryFilterRepository.findByAssociatedEntityOrderByOrdinal(sourceFeed);
	}
	
	
	@Transactional
	public List<SourceFeedEntryFilter> updateSourceFeedEntryFilters(
			EntryFilterList<SourceFeedEntryFilter, SourceFeed> filterList, SourceFeed sourceFeed) {
		return processFilterList(filterList, sourceFeedEntryFilterRepository, Objects.requireNonNull(sourceFeed), 
				SourceFeedEntryFilter::new);
	}
	
	
	public void deleteDigestEntryFilters(long digetstId) {
		digestEntryFilterRepository.deleteByAssociatedEntityId(digetstId);
	}
	
	
	public void deleteSourceFeedEntryFilters(long sourceFeedId) {
		sourceFeedEntryFilterRepository.deleteByAssociatedEntityId(sourceFeedId);
	}
	
	
	private static <T extends EntryFilter<E>, E> List<T> processFilterList(
			EntryFilterList<T, E> filterList,
			EntryFilterRepository<T, E> entryFilterRepository,
			E associatedEntity,
			Supplier<T> newFilterSupplier) {
		var submittedFilters = filterList.filters().stream()
				.sorted(Comparator.comparing(EntryFilterModel::ordinal))
				.collect(toList());
		
		validateConnectives(submittedFilters);
		
		var existingFiltersById = entryFilterRepository.findByAssociatedEntityAndMapById(associatedEntity);
		
		var filtersToDelete = findFiltersToDelete(submittedFilters, existingFiltersById.values());
		var filtersToSave = prepareFiltersToSave(submittedFilters, existingFiltersById, newFilterSupplier, associatedEntity);
		
		entryFilterRepository.deleteAll(filtersToDelete);
		
		return entryFilterRepository.saveAll(filtersToSave);
	}
	
	
	private static <T extends EntryFilter<E>, E> List<T> findFiltersToDelete(
			List<? extends EntryFilterModel<T, E>> submittedFilters, Collection<T> existingFilters) {
		var submittedFiltersIds = submittedFilters.stream()
				.<Long>map(EntryFilterModel::id)
				.filter(Objects::nonNull)
				.collect(toSet());
		return existingFilters.stream()
				.filter(entryFilter -> !submittedFiltersIds.contains(entryFilter.getId()))
				.collect(toList());
	}
	
	
	private static <T extends EntryFilter<E>, E> List<T> prepareFiltersToSave(
			List<? extends EntryFilterModel<T, E>> submittedFilters,
			Map<Long, T> existingFiltersById,
			Supplier<T> newFilterSupplier,
			E associatedEntity) {
		var filtersToSave = new ArrayList<T>();
		
		for (var filterData : submittedFilters) {
			T entryFilter;
			var id = filterData.id();
			if (id == null) {
				entryFilter = newFilterSupplier.get();
				entryFilter.setAssociatedEntity(associatedEntity);
			}
			else {
				entryFilter = existingFiltersById.get(id);
				if (entryFilter == null)
					throw new EntryFilterException(format("Filter with id '%d' not found", id));
			}
			entryFilter.setFilterData(filterData);
			filtersToSave.add(entryFilter);
		}
		
		normalizeOrdinals(filtersToSave);
		
		return filtersToSave;
	}
	
	
	private static void validateConnectives(List<? extends EntryFilterModel<?,?>> submittedFilters) {
		for (int i = 0; i < submittedFilters.size(); i++) {
			var filterData = submittedFilters.get(i);
			if (i == submittedFilters.size() - 1) {
				if (filterData.connective() != null)
					throw new EntryFilterException("Last filter in filter list cannot have connective");
			}
			else {
				if (filterData.connective() == null)
					throw new EntryFilterException(
							format("Filter at index %d in filter list contains no connective", i));
			}
		}
	}

	
	private static void normalizeOrdinals(List<? extends EntryFilter<?>> filters) {
		for (int i = 0; i < filters.size(); i++)
			filters.get(i).setOrdinal(i + 1);
	}

}
