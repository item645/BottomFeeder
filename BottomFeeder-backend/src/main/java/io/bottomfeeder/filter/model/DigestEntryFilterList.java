package io.bottomfeeder.filter.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.filter.DigestEntryFilter;

/**
 * A container for the list of digest entry filters.
 */
public record DigestEntryFilterList(
		
		@Valid
		@NotNull(message = "{validation.entry-filter-list.digest-filters.null}")
		List<DigestEntryFilterData> filters)

implements EntryFilterList<DigestEntryFilter, Digest> {
}
