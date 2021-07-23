package io.bottomfeeder.api;

import static io.bottomfeeder.config.Constants.API_URL_BASE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bottomfeeder.api.model.AccountResponse;
import io.bottomfeeder.api.model.AuthenticationRequest;
import io.bottomfeeder.api.model.PasswordChangeRequest;
import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.api.model.SignupRequest;
import io.bottomfeeder.security.auth.AuthenticationService;
import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserService;

/**
 * REST controller for performing authentication/logout and managing the account of currently
 * authenticated user.
 */
@RestController
@RequestMapping(API_URL_BASE)
class AccountController {

	private final AuthenticationService authenticationService;
	private final UserService userService;
	
	
	public AccountController(UserService userService, AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
		this.userService = userService;
	}


	@PostMapping("/authenticate")
	public Response<AccountResponse> authenticate(@Valid @RequestBody AuthenticationRequest authRequest, 
			HttpServletRequest request, HttpServletResponse response) {
		authenticationService.authenticate(authRequest.login(), authRequest.password(), request, response);
		return createAccountResponse(userService.getUser(authRequest.login()));
	}
	
	
	@PostMapping("/signup")
	public Response<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
		var user = userService.signup(signupRequest.login(), signupRequest.password());
		return new Response<>(String.format("User '%s' registered successfully", user.getLogin()));
	}
	
	
	@PutMapping("/account/change-password")
	@Transactional
	public Response<Void> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest,
			HttpServletRequest request, HttpServletResponse response) {
		userService.changeUserPassword(userService.getAuthenticatedUser(), 
				passwordChangeRequest.currentPassword(), passwordChangeRequest.newPassword());
		authenticationService.logout(request, response);
		return new Response<>("Password changed successfully");
	}
	
	
	@GetMapping("/account")
	public Response<AccountResponse> getAccount() {
		return createAccountResponse(userService.getAuthenticatedUser());
	}
	
	
	@PostMapping("/logout")
	public Response<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authenticationService.logout(request, response);
		return new Response<>();
	}
	
	
	private static Response<AccountResponse> createAccountResponse(User user) {
		return new Response<>(new AccountResponse(user));
	}
	
}
