package io.bottomfeeder.api.model;

import static io.bottomfeeder.digest.Digest.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.digest.Digest;

/**
 * Contains request data for creating new digest or updating existing one.
 */
public record DigestRequest(
		
		Long id,
		
		@NotNull(message = VALIDATION_TITLE_NULL)
		@Size(message = VALIDATION_TITLE_SIZE, min = TITLE_MIN_SIZE, max = TITLE_MAX_SIZE)
		String title,
		
		@Min(message = VALIDATION_MAX_ITEMS_MIN, value = MAX_ITEMS_MIN)
		@Max(message = VALIDATION_MAX_ITEMS_MAX, value = MAX_ITEMS_MAX)
		int maxItems,
		
		boolean isPrivate)

implements EntityModel<Digest> {

	@Override
	public Long entityId() {
		return id();
	}
	
	@Override
	public Class<Digest> entityClass() {
		return Digest.class;
	}
}
