package io.bottomfeeder.security;

/**
 * Enumerates roles that can be assigned to application users.
 */
public enum Role {
	
	ADMIN (Name.ADMIN),
	USER  (Name.USER);
	
	Role(String roleName) {
		if (!this.name().equals(roleName))
			throw new AssertionError();
	}
	
	/**
	 * Constants for role names to be used as values in Spring Security annotations.
	 */
	public static final class Name {
		private Name() {};
		
		public static final String ADMIN = "ADMIN";
		public static final String USER = "USER";
	}
}
