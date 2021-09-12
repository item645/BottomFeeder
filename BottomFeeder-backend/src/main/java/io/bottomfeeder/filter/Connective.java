package io.bottomfeeder.filter;

import java.util.function.Predicate;

/**
 * Logical connectives used to compose multiple filters.
 */
public enum Connective {
	
	AND {
		@Override
		<T> Predicate<T> compose(Predicate<T> currentFilter, Predicate<T> nextFilter) {
			return currentFilter.and(nextFilter);
		}
	},
	OR {
		@Override
		<T> Predicate<T> compose(Predicate<T> currentFilter, Predicate<T> nextFilter) {
			return currentFilter.or(nextFilter);
		}
	};
	
	abstract <T> Predicate<T> compose(Predicate<T> currentFilter, Predicate<T> nextFilter);
}
