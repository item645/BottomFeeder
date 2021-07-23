package io.bottomfeeder.security.permission;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.bottomfeeder.digest.DigestRepository;
import io.bottomfeeder.sourcefeed.SourceFeedRepository;
import io.bottomfeeder.user.User;

/**
 * A service that implements actual permission-checking logic for determining the rights
 * that specified user has for target entity.
 */
@Service
class PermissionService {
	
	private final DigestRepository digestRepository;
	private final SourceFeedRepository sourceFeedRepository;

	
	public PermissionService(DigestRepository digestRepository, SourceFeedRepository sourceFeedRepository) {
		this.digestRepository = digestRepository;
		this.sourceFeedRepository = sourceFeedRepository;
	}
	
	
	public boolean hasDigestPermission(Object digestId, User user, Permission permission) {
		Objects.requireNonNull(digestId, "Digest ID cannot be null");
		Objects.requireNonNull(permission, "Permission cannot be null");
		
		return switch (permission) {
			case READ, UPDATE, DELETE -> {
				if (user != null) {
					if (user.isAdmin()) {
						yield true;
					}
					else {
						var id = castId(digestId, Number.class).longValue();
						yield digestRepository.isDigestOwner(id, user.getId());
					}
				}
				else {
					yield false;
				}
			}
			case READ_DIGEST_FEED -> {
				if (user != null && user.isAdmin()) {
					yield true;
				}
				else {
					var externalId = castId(digestId, String.class);
					var userId = user != null ? user.getId() : null;
					yield digestRepository.canAccessDigestFeed(externalId, userId);
				}
				
			} 
			default -> throw unsupportedPermissionError("digest", permission);
		};
	}
	
	
	public boolean hasSourceFeedPermission(Object sourceFeedId, User user, Permission permission) {
		Objects.requireNonNull(permission, "Permission cannot be null");
		
		if (user != null) {
			return user.isAdmin() || switch (permission) {
				case READ, UPDATE, DELETE -> {
					var id = castId(sourceFeedId, Number.class).longValue();
					yield sourceFeedRepository.isSourceFeedDigestOwner(id, user.getId());
				}
				default -> throw unsupportedPermissionError("source feed", permission);
			};		
		}
		else {
			return false;
		}
	}
	
	
	private static <T> T castId(Object id, Class<T> expectedType) {
		if (!expectedType.isInstance(id))
			throw new IllegalArgumentException(String.format("Invalid ID type: %s. Expected type: %s", 
					id != null ? id.getClass() : "null", expectedType));
		return expectedType.cast(id);
	}	
	
	
	private static IllegalArgumentException unsupportedPermissionError(String entityName, Permission permission) {
		return new IllegalArgumentException(String.format("Unsupported %s permission: %s", entityName, permission));
	}
	
}
