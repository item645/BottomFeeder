package io.bottomfeeder.security.permission;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.filter.DigestEntryFilter;
import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserService;

/**
 * Default permission evaluator implementation which is used to evaluate permission expressions
 * for {@code @PreAuthorize/PostAuthorize}-annotated methods. 
 * This implementation mostly determines the target entity and then delegates to {@code PermissionService}
 * to perform actual permission checks.
 */
class DefaultPermissionEvaluator implements PermissionEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPermissionEvaluator.class);
	
	private final PermissionService permissionService;
	private final UserService userService;

	
	public DefaultPermissionEvaluator(PermissionService permissionService, UserService userService) {
		this.permissionService = permissionService;
		this.userService = userService;
	}


	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return hasPermission(getUser(authentication), targetDomainObject, (Permission) permission);
	}
	

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, 
			String targetType, Object permission) {
		return hasPermission(getUser(authentication), targetId, getTargetEntityClass(targetType), (Permission) permission);
	}

	
	private boolean hasPermission(User user, Object targetDomainObject, Permission permission) {
		if (targetDomainObject instanceof EntityModel<?> entityModel) {
			return hasPermission(user, entityModel.entityId(), entityModel.entityClass(), permission);
		}
		else if (targetDomainObject instanceof Collection<?> collection) {
			// For collection, perform short-circuit recursive check of given permission for
			// all of its elements. The permission is only granted for collection if it is 
			// granted for each element.
			for (var object : collection) {
				if (!hasPermission(user, object, permission))
					return false;
			}
			return true;
		}
		else {
			logger.warn(String.format("Unsupported target domain object type: %s", 
					targetDomainObject != null ? targetDomainObject.getClass() : "null"));
			return false;
		}
	}
	
	
	private boolean hasPermission(User user, Object targetId, Class<?> targetEntityClass, Permission permission) {
		if (targetId == null) {
			return false;
		}
		else {
			if (targetEntityClass == Digest.class) {
				return permissionService.hasDigestPermission(targetId, user, permission);
			}
			else if (targetEntityClass == SourceFeed.class) {
				return permissionService.hasSourceFeedPermission(targetId, user, permission);
			}
			else if (targetEntityClass == DigestEntryFilter.class) {
				return permissionService.hasDigestEntryFilterPermission(targetId, user, permission);
			}
			else if (targetEntityClass == SourceFeedEntryFilter.class) {
				return permissionService.hasSourceFeedEntryFilterPermission(targetId, user, permission);
			}
			else {
				logger.warn(String.format("Unsupported target entity type: %s", targetEntityClass));
				return false;
			}
		}
	}

	
	private User getUser(Authentication authentication) {
		return userService.getAuthenticatedUserOrNull(authentication);
	}
	
	
	private static Class<?> getTargetEntityClass(String targetType) {
		try {
			return Class.forName(targetType);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
