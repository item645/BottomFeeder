package io.bottomfeeder.filter;

import java.util.List;

/**
 * Represents a container for the list of entry filters associated with particular entity.
 * 
 * @param <T> the type of entry filter entity
 * @param <E> the type of entity that filter is associated with
 */
public interface EntryFilterList<T extends EntryFilter<E>, E> {

	List<? extends EntryFilterModel<T, E>> filters();

}
