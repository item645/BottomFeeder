package io.bottomfeeder.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.bottomfeeder.security.Role;

/**
 * Spring Data repository for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findOneByLogin(String login);
	
	boolean existsByLoginIgnoreCase(String login);
	
	int countByRole(Role role);	
}
