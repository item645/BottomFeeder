package io.bottomfeeder.api.model;

import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_NULL;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_SIZE;
import static io.bottomfeeder.user.User.PASSWORD_MIN_SIZE;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Contains request data for changing user password.
 */
public record PasswordChangeRequest(
		
		@NotNull(message = VALIDATION_PASSWORD_NULL)
		String currentPassword,
		
		@NotNull(message = VALIDATION_PASSWORD_NULL)
		@Size(message = VALIDATION_PASSWORD_SIZE, min = PASSWORD_MIN_SIZE)
		String newPassword) {
}
