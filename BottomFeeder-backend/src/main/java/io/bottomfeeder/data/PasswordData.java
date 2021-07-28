package io.bottomfeeder.data;

import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_NULL;

import javax.validation.constraints.NotNull;

import io.bottomfeeder.user.User;

/**
 * Container for imported or exported data representing user password.
 */
record PasswordData(
		
		@NotNull(message = VALIDATION_PASSWORD_NULL)
		String value,
		
		@NotNull(message = "{validation.password-data.password-format.null}")
		PasswordFormat format) {
	
	PasswordData(User user) {
		this(user.getPassword(), PasswordFormat.BCRYPT_HASH);
	}
}
