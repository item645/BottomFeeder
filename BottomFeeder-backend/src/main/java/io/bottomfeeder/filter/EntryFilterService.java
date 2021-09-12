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
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rometools.rome.feed.synd.SyndEntry;

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
		// TODO check for duplicate ids
		for (int i = 0; i < submittedFilters.size(); i++)
			validateFilterData(submittedFilters.get(i), i, i == submittedFilters.size() - 1);
	}
	
	
	private static void validateFilterData(EntryFilterModel<?,?> filterData, int filterIndex, boolean isLast) {
		var element = filterData.element();
		if (element == null)
			throw invalidFilterError(filterIndex, "contains no element");
		
		var elementDataType = element.dataType();
		
		var condition = filterData.condition();
		if (!DataTypeCondition.isValid(elementDataType, condition))
			throw invalidFilterError(filterIndex, "contains unsupported condition %s for element %s of data type %s",
					condition, element, elementDataType);
		
		var value = filterData.value();
		if (value == null)
			throw invalidFilterError(filterIndex, "contains null value");
		if (elementDataType == ElementDataType.DATE_TIME) {
			if (!DateTimeUtils.isValidDateTime(value))
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
	
	
	private static EntryFilterException invalidFilterError(int filterIndex, 
			String descriptionTemplate, Object... args) {
		var message = format("Filter at index %d %s", filterIndex, format(descriptionTemplate, args));
		return new EntryFilterException(message);
	}

	
	private static void normalizeOrdinals(List<? extends EntryFilter<?>> filters) {
		for (int i = 0; i < filters.size(); i++)
			filters.get(i).setOrdinal(i + 1);
	}

	
	public Predicate<SyndEntry> getDigestEntryFilterChain(Digest digest) {
		return createEntryFilterChain(getDigestEntryFilters(digest));
	}
	
	
	public Predicate<SyndEntry> getSourceFeedEntryFilterChain(SourceFeed sourceFeed) {
		return createEntryFilterChain(getSourceFeedEntryFilters(sourceFeed));
	}

	
	private static Predicate<SyndEntry> createEntryFilterChain(List<? extends EntryFilter<?>> entryFilters) {
		if (entryFilters.isEmpty()) {
			return null;
		}
		else {
			var filterChain = createPredicate(entryFilters.get(0));
			if (entryFilters.size() > 1) {
				for (int i = 1; i < entryFilters.size(); i++) {
					var connective = entryFilters.get(i - 1).getConnective();
					filterChain = connective.compose(filterChain, createPredicate(entryFilters.get(i)));
				}
			}
			return filterChain;
		}
	}
	
	
	private static Predicate<SyndEntry> createPredicate(EntryFilter<?> entryFilter) {
		return syndEntry -> {
			if (syndEntry == null) {
				return false;
			}
			else {
				var element = entryFilter.getElement();
				var evaluator = DataTypeCondition.of(element.dataType(), entryFilter.getCondition()).conditionEvaluator();
				return evaluator.evaluate(element.readValue(syndEntry), entryFilter.getValue());
			}
		};
	}
	
}
