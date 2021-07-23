package io.bottomfeeder.api.model;

import io.bottomfeeder.digest.Digest;

/**
 * Contains simplified response data for the digest, including only its id, title and
 * owner login.
 * This is mostly intended for digest selection lists on frontend. 
 */
public record DigestTitleResponse(long id, String ownerLogin, String title) {
	
	public DigestTitleResponse(Digest digest) {
		this(digest.getId(), digest.getOwner().getLogin(), digest.getTitle());
	}
	
}
