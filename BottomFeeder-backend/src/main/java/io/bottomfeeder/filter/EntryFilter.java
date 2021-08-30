package io.bottomfeeder.filter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Base class for entry filter entity implementations, defines common mapped properties.
 * 
 * @param <E> the type of entity this filter is associated with
 */
@MappedSuperclass
public abstract class EntryFilter<E> {

	public static final int ORDINAL_MIN = 1;
	
	public static final int ELEMENT_MIN_SIZE = 1;
	public static final int ELEMENT_MAX_SIZE = 100;
	
	public static final int VALUE_MAX_SIZE = 200;
	
	public static final String VALIDATION_ORDINAL_MIN = "{validation.entry-filter.ordinal.min}";
	public static final String VALIDATION_ELEMENT_NULL = "{validation.entry-filter.element.null}";
	public static final String VALIDATION_ELEMENT_SIZE = "{validation.entry-filter.element.size}";
	public static final String VALIDATION_CONDITION_NULL = "{validation.entry-filter.condition.null}";
	public static final String VALIDATION_VALUE_NULL = "{validation.entry-filter.value.null}";
	public static final String VALIDATION_VALUE_SIZE = "{validation.entry-filter.value.size}";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Min(message = VALIDATION_ORDINAL_MIN, value = ORDINAL_MIN)
	@Column(nullable = false)
	private int ordinal;
	
	@NotNull(message = VALIDATION_ELEMENT_NULL)
	@Size(message = VALIDATION_ELEMENT_SIZE, min = ELEMENT_MIN_SIZE, max = ELEMENT_MAX_SIZE)
	@Column(length = ELEMENT_MAX_SIZE, nullable = false)
	private String element;
	
	@NotNull(message = VALIDATION_CONDITION_NULL)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Condition condition;
	
	@NotNull(message = VALIDATION_VALUE_NULL)
	@Size(message = VALIDATION_VALUE_SIZE, max = VALUE_MAX_SIZE)
	@Column(length = VALUE_MAX_SIZE, nullable = false)
	private String value;
	
	@Enumerated(EnumType.STRING)
	private Connective connective;

	EntryFilter() {}
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Connective getConnective() {
		return connective;
	}

	public void setConnective(Connective connective) {
		this.connective = connective;
	}
	
	public abstract E getAssociatedEntity();
	
	public abstract void setAssociatedEntity(E associatedEntity);
	
	@Transient
	public void setFilterData(EntryFilterModel<?,?> filterData) {
		id = filterData.id();
		ordinal = filterData.ordinal();
		element = filterData.element();
		condition = filterData.condition();
		value = filterData.value();
		connective = filterData.connective();
	}
	
}