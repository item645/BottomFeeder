package io.bottomfeeder.api.model;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.user.User;

/**
 * Contains response data for user.
 */
public record UserResponse(
		long id,
		String login,
		Role role) 

implements EntityModel<User> {
	
	public UserResponse(User user) {
		this(user.getId(), user.getLogin(), user.getRole());
	}
	
	@Override
	public Long entityId() {
		return id();
	}

	@Override
	public Class<User> entityClass() {
		return User.class;
	}
}
