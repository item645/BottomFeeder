package io.bottomfeeder.api.model;

import java.time.Instant;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.digest.Digest;

/**
 * Contains response data for the digest.
 */
public record DigestResponse(
		long id,
		String externalId,
		String rssLink,
		String atomLink,
		String ownerLogin,
		String title,
		Instant creationDate,
		int maxEntries,
		boolean isPrivate)

implements EntityModel<Digest> {

	public DigestResponse(Digest digest, String rssLink, String atomLink) {
		this(
			digest.getId(),
			digest.getExternalId(),
			rssLink,
			atomLink,
			digest.getOwner().getLogin(),
			digest.getTitle(),
			digest.getCreationDate(),
			digest.getMaxEntries(),
			digest.isPrivate()
			);
	}
	
	@Override
	public Long entityId() {
		return id();
	}
	
	@Override
	public Class<Digest> entityClass() {
		return Digest.class;
	}
}
