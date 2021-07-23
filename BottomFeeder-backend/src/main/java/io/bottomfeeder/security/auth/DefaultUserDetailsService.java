package io.bottomfeeder.security.auth;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserRepository;

/**
 * Default user details service implementation used to find application user by login
 * during authentication and build UserDetails instance from obtained User entity.
 */
@Service
class DefaultUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;


	public DefaultUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}


	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		return userRepository.findOneByLogin(login)
				.map(DefaultUserDetailsService::createUserDetails)
				.orElseThrow(() -> new UsernameNotFoundException(String.format("Login '%s' not found", login)));
	}

	
	private static UserDetails createUserDetails(User user) {
		return new org.springframework.security.core.userdetails.User(
				user.getLogin(), user.getPassword(), List.of(new SimpleGrantedAuthority(user.getRoleName())));
	}
	
}
