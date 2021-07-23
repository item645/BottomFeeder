package io.bottomfeeder.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.bottomfeeder.security.Role;

/**
 * Represents a user account in the application.
 */
@Entity
@Table(name = "bf_user")
public class User {

	public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
	public static final int LOGIN_MIN_SIZE = 3;
	public static final int LOGIN_MAX_SIZE = 50;
	
	public static final int PASSWORD_MIN_SIZE = 5;
	
	// Borrowed from private implementation of org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
	public static final String PASSWORD_HASH_REGEX = "\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}";
	public static final int PASSWORD_HASH_SIZE = 60; // versions $2a, $2b, $2y; if version $2 is used, should be 59
	
	public static final String VALIDATION_LOGIN_NULL = "{validation.login.null}";
	public static final String VALIDATION_LOGIN_REGEX = "{validation.login.regex}";
	public static final String VALIDATION_LOGIN_SIZE = "{validation.login.size}";
	public static final String VALIDATION_PASSWORD_NULL = "{validation.password.null}";
	public static final String VALIDATION_PASSWORD_SIZE = "{validation.password.size}";
	public static final String VALIDATION_PASSWORD_HASH_NULL = "{validation.password-hash.null}";
	public static final String VALIDATION_PASSWORD_HASH_REGEX = "{validation.password-hash.regex}";
	public static final String VALIDATION_ROLE_NULL = "{validation.role.null}";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = VALIDATION_LOGIN_NULL)
	@Pattern(message = VALIDATION_LOGIN_REGEX, regexp = LOGIN_REGEX)
	@Size(message = VALIDATION_LOGIN_SIZE, min = LOGIN_MIN_SIZE, max = LOGIN_MAX_SIZE)
	@Column(length = LOGIN_MAX_SIZE, unique = true, nullable = false)
	private String login;
	
	@NotNull(message = VALIDATION_PASSWORD_HASH_NULL)
	@Pattern(message = VALIDATION_PASSWORD_HASH_REGEX, regexp = PASSWORD_HASH_REGEX)
	@Column(name = "password_hash", length = PASSWORD_HASH_SIZE, nullable = false)
	private String password;

	@NotNull(message = VALIDATION_ROLE_NULL)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;
	
	
	public User() {}
	
	public User(String login, String password, Role role) {
		this.login = login;
		this.password = password;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	@Transient
	public String getRoleName() {
		return role.name();
	}
	
	@Transient
	public boolean isAdmin() {
		return role == Role.ADMIN;
	}
	
}
