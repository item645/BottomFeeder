package io.bottomfeeder.filter.model;

import static io.bottomfeeder.filter.EntryFilter.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.Element;
import io.bottomfeeder.filter.EntryFilter;

/**
 * Base class for entry filter model implementations that act as simple JSON-convertible
 * containers for filter data.
 * 
 * @param <T> the type of entry filter entity
 * @param <E> the type of entity that filter is associated with
 */
abstract class AbstractEntryFilterData<T extends EntryFilter<E>, E> implements EntryFilterModel<T, E> {

	private final Long id;
	
	@Min(message = VALIDATION_ORDINAL_MIN, value = ORDINAL_MIN)
	private final int ordinal;
	
	@NotNull(message = VALIDATION_ELEMENT_NULL)
	private final Element element;
	
	@NotNull(message = VALIDATION_CONDITION_NULL)
	private final Condition condition;
	
	@NotNull(message = VALIDATION_VALUE_NULL)
	@Size(message = VALIDATION_VALUE_SIZE, max = VALUE_MAX_SIZE)
	private final String value;
		
	private final Connective connective;
	
	
	AbstractEntryFilterData(Long id, int ordinal, Element element, Condition condition, 
			String value, Connective connective) {
		this.id = id;
		this.ordinal = ordinal;
		this.element = element;
		this.condition = condition;
		this.value = value;
		this.connective = connective;
	}


	AbstractEntryFilterData(T entryFilter) {
		this(
			entryFilter.getId(),
			entryFilter.getOrdinal(),
			entryFilter.getElement(),
			entryFilter.getCondition(),
			entryFilter.getValue(),
			entryFilter.getConnective()
			);
	}


	@Override
	@JsonGetter("id")
	public Long id() {
		return id;
	}

	@Override
	@JsonGetter("ordinal")
	public int ordinal() {
		return ordinal;
	}

	@Override
	@JsonGetter("element")
	public Element element() {
		return element;
	}

	@Override
	@JsonGetter("condition")
	public Condition condition() {
		return condition;
	}

	@Override
	@JsonGetter("value")
	public String value() {
		return value;
	}

	@Override
	@JsonGetter("connective")
	public Connective connective() {
		return connective;
	}
	
}
