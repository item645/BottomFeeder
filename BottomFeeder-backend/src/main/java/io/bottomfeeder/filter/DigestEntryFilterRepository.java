package io.bottomfeeder.filter;

import io.bottomfeeder.digest.Digest;

/**
 * Spring Data repository for digest entry filters.
 */
public interface DigestEntryFilterRepository extends EntryFilterRepository<DigestEntryFilter, Digest> {
}
