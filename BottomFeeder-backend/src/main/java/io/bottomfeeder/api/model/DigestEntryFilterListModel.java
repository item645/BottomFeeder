package io.bottomfeeder.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.filter.DigestEntryFilter;
import io.bottomfeeder.filter.EntryFilterList;

/**
 * Contains request or response data for the list of digest entry filters.
 */
public record DigestEntryFilterListModel(
		
		@Valid
		@NotNull
		List<DigestEntryFilterModel> filters)

implements EntryFilterList<DigestEntryFilter, Digest> {
}
