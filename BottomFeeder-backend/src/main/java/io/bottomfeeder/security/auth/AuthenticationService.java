package io.bottomfeeder.security.auth;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Service;

import io.bottomfeeder.config.Constants;

/**
 * A service that provides functionality for users authentication and session management
 * in the application.
 */
@Service
public class AuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
	
	private final AuthenticationManager authenticationManager;
	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
	private final SessionRegistry sessionRegistry;
	private final LogoutHandler logoutHandler;
	
	
	public AuthenticationService(
			AuthenticationManager authenticationManager,
			SessionAuthenticationStrategy sessionAuthenticationStrategy, 
			SessionRegistry sessionRegistry, 
			LogoutHandler logoutHandler) {
		this.authenticationManager = authenticationManager;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
		this.sessionRegistry = sessionRegistry;
		this.logoutHandler = logoutHandler;
	}


	public void authenticate(String login, String password, 
			HttpServletRequest request, HttpServletResponse response) {
		var token = new UsernamePasswordAuthenticationToken(login, password);
		var authentication = this.authenticationManager.authenticate(token);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
	}
	
	
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
	}
	
	
	public Optional<String> getAuthenticatedLogin() {
		return getAuthenticatedLogin(SecurityContextHolder.getContext().getAuthentication());
	}

	
	public Optional<String> getAuthenticatedLogin(Authentication authentication) {
		return Optional.ofNullable(authentication)
					.map(auth -> getPrincipalLogin(auth.getPrincipal()))
					.filter(AuthenticationService::isNotAnonymousLogin);
	}
	
	
	public void expireSessions(String login) {
		sessionRegistry.getAllPrincipals().stream()
			.filter(principal -> principalMatches(login, principal))
			.findFirst()
			.ifPresent(this::expirePrincipalSessions);
	}
	
	
	private void expirePrincipalSessions(Object principal) {
		logger.debug(String.format("Expiring sessions for: %s", principal));
		sessionRegistry.getAllSessions(principal, false).forEach(SessionInformation::expireNow);
	}
	
	
	private static String getPrincipalLogin(Object principal) {
		if (principal instanceof UserDetails ud) {
			return ud.getUsername();
		}
		else if (principal instanceof String login) {
			return login;
		}
		else {
			logger.warn(String.format("Unrecognized principal type: %s", 
					Objects.toString(principal == null ? null : principal.getClass())));
			return null;
		}
	}
	
	
	private static boolean principalMatches(String login, Object principal) {
		return login.equals(getPrincipalLogin(principal));
	}
	
	
	private static boolean isNotAnonymousLogin(String login) {
		return !Constants.ANONYMOUS_PRINCIPAL.equals(login);
	}
	
}
