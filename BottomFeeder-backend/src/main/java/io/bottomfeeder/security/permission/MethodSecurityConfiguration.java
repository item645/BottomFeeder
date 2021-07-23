package io.bottomfeeder.security.permission;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import io.bottomfeeder.user.UserService;

/**
 * Configures method-level security for application.
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

	private final PermissionService permissionService;
	private final UserService userService;
	
	
	public MethodSecurityConfiguration(PermissionService permissionService, UserService userService) {
		this.permissionService = permissionService;
		this.userService = userService;
	}

	
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		var expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(new DefaultPermissionEvaluator(permissionService, userService));
		return expressionHandler;
	}
	
}
