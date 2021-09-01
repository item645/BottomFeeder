package io.bottomfeeder.security.permission;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.bottomfeeder.digest.DigestRepository;
import io.bottomfeeder.filter.DigestEntryFilterRepository;
import io.bottomfeeder.filter.EntryFilterRepository;
import io.bottomfeeder.filter.SourceFeedEntryFilterRepository;
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
	private final DigestEntryFilterRepository digestEntryFilterRepository;
	private final SourceFeedEntryFilterRepository sourceFeedEntryFilterRepository;

	
	public PermissionService(
			DigestRepository digestRepository, 
			SourceFeedRepository sourceFeedRepository, 
			DigestEntryFilterRepository digestEntryFilterRepository, 
			SourceFeedEntryFilterRepository sourceFeedEntryFilterRepository) {
		this.digestRepository = digestRepository;
		this.sourceFeedRepository = sourceFeedRepository;
		this.digestEntryFilterRepository = digestEntryFilterRepository;
		this.sourceFeedEntryFilterRepository = sourceFeedEntryFilterRepository;
	}
	
	
	public boolean hasDigestPermission(Object digestId, User user, Permission permission) {
		ensureNonNullPermission(permission);
		
		return switch (permission) {
			case READ, UPDATE, DELETE -> {
				if (user != null) {
					if (user.isAdmin()) {
						yield true;
					}
					else {
						var id = castIdToLong(digestId);
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
		ensureNonNullPermission(permission);
		
		if (user != null) {
			return user.isAdmin() || switch (permission) {
				case READ, UPDATE, DELETE -> {
					var id = castIdToLong(sourceFeedId);
					yield sourceFeedRepository.isSourceFeedDigestOwner(id, user.getId());
				}
				default -> throw unsupportedPermissionError("source feed", permission);
			};		
		}
		else {
			return false;
		}
	}
	
	
	public boolean hasDigestEntryFilterPermission(Object digestEntryFilterId, User user, Permission permission) {
		return hasEntryFilterPermission(castIdToLong(digestEntryFilterId), user, permission, 
				digestEntryFilterRepository);
	}
	
	
	public boolean hasSourceFeedEntryFilterPermission(Object sourceFeedEntryFilterId, User user, Permission permission) {
		return hasEntryFilterPermission(castIdToLong(sourceFeedEntryFilterId), user, permission,
				sourceFeedEntryFilterRepository);
	}
	
	
	private static boolean hasEntryFilterPermission(long entryFilterId, User user, Permission permission, 
			EntryFilterRepository<?,?> entryFilterRepository) {
		ensureNonNullPermission(permission);
		
		if (user != null) {
			return user.isAdmin() || switch (permission) {
				case READ, UPDATE, DELETE -> entryFilterRepository.isAssociatedEntityOwner(entryFilterId, user.getId());
				default -> throw unsupportedPermissionError("entry filter", permission);
			};
		}
		else {
			return false;
		}
	}

	
	private static void ensureNonNullPermission(Permission permission) {
		Objects.requireNonNull(permission, "Permission cannot be null");
	}
	
	
	private static long castIdToLong(Object id) {
		return castId(id, Number.class).longValue();
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
