package io.bottomfeeder.api.model;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.filter.Condition;
import io.bottomfeeder.filter.Connective;
import io.bottomfeeder.filter.DigestEntryFilter;

/**
 * Contains request or response data for individual digest entry filter.
 */
public final class DigestEntryFilterModel extends AbstractEntryFilterModel<DigestEntryFilter, Digest> {
	
	public DigestEntryFilterModel(Long id, int ordinal, String element, Condition condition, 
			String value, Connective connective) {
		super(id, ordinal, element, condition, value, connective);
	}

	public DigestEntryFilterModel(DigestEntryFilter entryFilter) {
		super(entryFilter);
	}

	@Override
	public Class<DigestEntryFilter> entityClass() {
		return DigestEntryFilter.class;
	}

}
