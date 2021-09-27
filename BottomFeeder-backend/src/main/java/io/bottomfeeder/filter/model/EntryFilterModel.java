package io.bottomfeeder.filter.model;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.Element;
import io.bottomfeeder.filter.EntryFilter;

/**
 * A model for entry filter providing access to properties common for all filter implementations.
 * 
 * @param <T> the type of entry filter entity
 * @param <E> the type of entity that filter is associated with
 */
public interface EntryFilterModel<T extends EntryFilter<E>, E> extends EntityModel<T> {

	Long id();
	
	int ordinal();
	
	Element element();
	
	Condition condition();
	
	String value();
	
	Connective connective();
	
	@Override
	default Long entityId() {
		return id();
	}
	
}
