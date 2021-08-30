package io.bottomfeeder.filter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.digest.Digest;

/**
 * Represents entry filter for digest.
 */
@Entity
@Table(name = "digest_entry_filter")
public class DigestEntryFilter extends EntryFilter<Digest> {

	private static final String VALIDATION_DIGEST_NULL = "{validation.entry-filter.digest.null}";

	@NotNull(message = VALIDATION_DIGEST_NULL)
	@ManyToOne(optional = false)
	@JoinColumn(name = "digest_id")
	private Digest associatedEntity;
	
	public DigestEntryFilter() {}
	
	
	@Override
	public Digest getAssociatedEntity() {
		return associatedEntity;
	}

	@Override
	public void setAssociatedEntity(Digest associatedEntity) {
		this.associatedEntity = associatedEntity;
	}
	
}
