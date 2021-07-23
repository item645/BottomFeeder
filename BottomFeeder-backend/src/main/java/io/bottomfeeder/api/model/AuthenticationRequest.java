package io.bottomfeeder.api.model;

import static io.bottomfeeder.user.User.VALIDATION_LOGIN_NULL;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_NULL;

import javax.validation.constraints.NotNull;

/**
 * Contains request data for performing authentication.
 */
public record AuthenticationRequest(
		
		@NotNull(message = VALIDATION_LOGIN_NULL)
		String login,
		
		@NotNull(message = VALIDATION_PASSWORD_NULL)
		String password) {
}
