package io.bottomfeeder.api.model;

import io.bottomfeeder.security.Role;
import io.bottomfeeder.user.User;

/**
 * Contains response data for current authenticated user account.
 */
public record AccountResponse(String login, Role role) {
	
	public AccountResponse(User user) {
		this(user.getLogin(), user.getRole());
	}
	
}
