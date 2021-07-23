package io.bottomfeeder.api.model;

import static io.bottomfeeder.user.User.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.bottomfeeder.base.EntityModel;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.user.User;

/**
 * Contains request data for creating new user or updating existing one.
 */
public record UserRequest(
		
		Long id,
		
		@NotNull(message = VALIDATION_LOGIN_NULL)
		@Pattern(message = VALIDATION_LOGIN_REGEX, regexp = LOGIN_REGEX)
		@Size(message = VALIDATION_LOGIN_SIZE, min = LOGIN_MIN_SIZE, max = LOGIN_MAX_SIZE)
		String login,
		
		@Size(message = VALIDATION_PASSWORD_SIZE, min = PASSWORD_MIN_SIZE)
		String password,
		
		@NotNull(message = VALIDATION_ROLE_NULL)
		Role role) 

implements EntityModel<User> {
	
	@Override
	public Long entityId() {
		return id();
	}

	@Override
	public Class<User> entityClass() {
		return User.class;
	}
}
