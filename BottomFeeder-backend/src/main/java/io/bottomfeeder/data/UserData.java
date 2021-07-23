package io.bottomfeeder.data;

import static io.bottomfeeder.user.User.*;

import java.util.Collection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.bottomfeeder.security.Role;

/**
 * Container for imported or exported data representing a single user together
 * with digests that this user owns.
 */
record UserData(
		
		@NotNull(message = VALIDATION_LOGIN_NULL)
		@Pattern(message = VALIDATION_LOGIN_REGEX, regexp = LOGIN_REGEX)
		@Size(message = VALIDATION_LOGIN_SIZE, min = LOGIN_MIN_SIZE, max = LOGIN_MAX_SIZE)
		String login,
		
		@NotNull(message = "{validation.user-data.password.null}")
		PasswordData password,
		
		@NotNull(message = VALIDATION_ROLE_NULL)
		Role role,
		
		@NotNull(message = "{validation.user-data.digests.null}")
		Collection<DigestData> digests) {
}
