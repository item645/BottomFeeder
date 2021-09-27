package io.bottomfeeder.filter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.DigestEntryFilter;
import io.bottomfeeder.filter.Element;

/**
 * Contains data for individual digest entry filter.
 */
public final class DigestEntryFilterData extends AbstractEntryFilterData<DigestEntryFilter, Digest> {

	@JsonCreator
	public DigestEntryFilterData(
			@JsonProperty("id") Long id,
			@JsonProperty("ordinal") int ordinal,
			@JsonProperty("element") Element element,
			@JsonProperty("condition") Condition condition,
			@JsonProperty("value") String value,
			@JsonProperty("connective") Connective connective) {
		super(id, ordinal, element, condition, value, connective);
	}
	
	public DigestEntryFilterData(DigestEntryFilter entryFilter) {
		super(entryFilter);
	}

	@Override
	public Class<DigestEntryFilter> entityClass() {
		return DigestEntryFilter.class;
	}

}
