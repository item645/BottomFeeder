package io.bottomfeeder.api.model;

import static io.bottomfeeder.user.User.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Contains request data for signing up in the application.
 */
public record SignupRequest(
		
		@NotNull(message = VALIDATION_LOGIN_NULL)
		@Pattern(message = VALIDATION_LOGIN_REGEX, regexp = LOGIN_REGEX)
		@Size(message = VALIDATION_LOGIN_SIZE, min = LOGIN_MIN_SIZE, max = LOGIN_MAX_SIZE)
		String login,
		
		@NotNull(message = VALIDATION_PASSWORD_NULL)
		@Size(message = VALIDATION_PASSWORD_SIZE, min = PASSWORD_MIN_SIZE)
		String password) {
}
