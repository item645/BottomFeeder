package io.bottomfeeder.filter;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

	private static final DateTimeFormatter DATE_VALUE_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final DateTimeFormatter DATE_TIME_VALUE_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	
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
		
		validateSubmittedFilters(submittedFilters);
		
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
	
	
	private static void validateSubmittedFilters(List<? extends EntryFilterModel<?,?>> submittedFilters) {
		for (int i = 0; i < submittedFilters.size(); i++)
			validateFilterData(submittedFilters.get(i), i, i == submittedFilters.size() - 1);
	}
	
	
	private static void validateFilterData(EntryFilterModel<?,?> filterData, int filterIndex, boolean isLast) {
		var element = filterData.element();
		if (element == null)
			throw invalidFilterError(filterIndex, "contains no element");
		
		var elementDataType = element.dataType();
		
		var condition = filterData.condition();
		if (!elementDataType.supportsCondition(condition))
			throw invalidFilterError(filterIndex, "contains unsupported condition %s for element %s of data type %s",
					condition, element, elementDataType);
		
		var value = filterData.value();
		if (value == null)
			throw invalidFilterError(filterIndex, "contains null value");
		if (elementDataType == ElementDataType.DATE_TIME) {
			if (!isValidDateTime(value))
				throw invalidFilterError(filterIndex, "contains invalid date/time value: %s", value);
		}
		
		var connective = filterData.connective();
		if (isLast) {
			if (connective != null)
				throw invalidFilterError(filterIndex, "(last) cannot have connective");
		}
		else {
			if (connective == null)
				throw invalidFilterError(filterIndex, "contains no connective");
		}
	}
	
	
	private static boolean isValidDateTime(String value) {
		return isValidDateTime(value, DATE_VALUE_PATTERN) || isValidDateTime(value, DATE_TIME_VALUE_PATTERN);
	}
	
	
	private static boolean isValidDateTime(String text, DateTimeFormatter format) {
		try {
			format.parse(text);
			return true;
		}
		catch (DateTimeParseException e) {
			return false;
		}
	}
	
	
	private static EntryFilterException invalidFilterError(int filterIndex, 
			String descriptionTemplate, Object... args) {
		var message = format("Filter at index %d %s", filterIndex, format(descriptionTemplate, args));
		return new EntryFilterException(message);
	}

	
	private static void normalizeOrdinals(List<? extends EntryFilter<?>> filters) {
		for (int i = 0; i < filters.size(); i++)
			filters.get(i).setOrdinal(i + 1);
	}

}
