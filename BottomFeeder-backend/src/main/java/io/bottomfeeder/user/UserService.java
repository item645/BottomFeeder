package io.bottomfeeder.user;

import static java.lang.String.format;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.security.auth.AuthenticationService;

/**
 * A service providing common functionality for managing application users.
 */
@Service
public class UserService {

	private final AuthenticationService authenticationService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final DigestService digestService;
	
	
	public UserService(
			@Lazy AuthenticationService authenticationService, // defer injection until servlet context is set
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			DigestService digestService) {
		this.authenticationService = authenticationService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.digestService = digestService;
	}

	
	public List<User> getAllUsers() {
		return userRepository.findAll(Sort.by("login"));
	}
	
	
	public User getUser(long id) {
		return userRepository.findById(id).orElseThrow(
				() -> new UserException(format("User with id '%d' not found", id)));
	}
	
	
	public User getUser(String login) {
		return userRepository.findOneByLogin(login).orElseThrow(
				() -> new UserException(format("User with login '%s' not found", login)));
	}
	
	
	public User getAuthenticatedUser() {
		return authenticationService.getAuthenticatedLogin()
				.map(this::getUser)
				.orElseThrow(UserService::notAuthenticatedError);
	}
	
	
	public User getAuthenticatedUserOrNull(Authentication authentication) {
		return authenticationService.getAuthenticatedLogin(authentication)
				.map(this::getUser)
				.orElse(null);
	}
	
	
	private static InsufficientAuthenticationException notAuthenticatedError() {
		return new InsufficientAuthenticationException("Not authenticated");
	}
	
	
	public boolean hasUsers() {
		return userRepository.count() > 0;
	}
	
	
	public User signup(String login, String password) {
		return saveNewUser(login, password, true, !hasUsers() ? Role.ADMIN : Role.USER);
	}
	
	
	public User createUser(String login, String password, Role role) {
		return createUser(login, password, true, role);
	}
	
	
	public User createUser(String login, String password, boolean hashPassword, Role role) {
		if (userRepository.existsByLoginIgnoreCase(login))
			throw new UserException(format("Login '%s' is already used", login));
		return saveNewUser(login, password, hashPassword, role);
	}
	
	
	private User saveNewUser(String login, String password, boolean hashPassword, Role role) {
		if (StringUtils.isBlank(password))
			throw new UserException("Password not specified");
		if (hashPassword)
			password = passwordEncoder.encode(password);
		
		return userRepository.save(new User(login, password, role));
	}
	
	
	@Transactional
	public User updateUser(long id, String newLogin, Role newRole) {
		var user = getUser(id);
		
		var login = user.getLogin();
		if (!login.equalsIgnoreCase(newLogin) && userRepository.existsByLoginIgnoreCase(newLogin))
			throw new UserException(format("Login '%s' is already used by different user", newLogin));
		
		var role = user.getRole();
		if (role != Objects.requireNonNull(newRole) && role == Role.ADMIN)
			checkOnlyOneAdminAccountLeft(login, "demoted to USER");
		
		user.setLogin(newLogin);
		user.setRole(newRole);
		user = userRepository.save(user);
		
		if (!login.equalsIgnoreCase(newLogin) || !role.equals(newRole))
			authenticationService.expireSessions(login);
		
		return user;
	}
	
	
	@Transactional
	public void deleteUser(long id) {
		var user = getUser(id);
		if (user.isAdmin())
			checkOnlyOneAdminAccountLeft(user.getLogin(), "deleted");
		
		digestService.deleteUserDigests(user);
		userRepository.delete(user);
		
		authenticationService.expireSessions(user.getLogin());
	}
	
	
	private void checkOnlyOneAdminAccountLeft(String currentUserLogin, String pendingAction) {
		if (userRepository.countByRole(Role.ADMIN) == 1)
			throw new UserException(format("User '%s' with role ADMIN cannot be %s "
					+ "because it is the only administrative account left in the system",
					currentUserLogin, pendingAction));
	}
	
	
	@Transactional
	public void changeUserPassword(User user, String currentPassword, String newPassword) {
		if (!passwordEncoder.matches(currentPassword, user.getPassword()))
			throw new UserException("Incorrect current password");
		
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
		authenticationService.expireSessions(user.getLogin());
	}
	
}
