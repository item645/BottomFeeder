package io.bottomfeeder.security.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Default authentication entry point implementation.
 */
@Component
class DefaultAuthenticationEntryPoint extends HandlerBase<AuthenticationException> 
	implements AuthenticationEntryPoint {
	
	DefaultAuthenticationEntryPoint(
			ApplicationContext applicationContext,
			HandlerExceptionResolver handlerExceptionResolver) {
		super(applicationContext, handlerExceptionResolver);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		var handlerMethod = getHandlerMethod("handleAuthenticationError", AuthenticationException.class);
		handlerExceptionResolver.resolveException(request, response, handlerMethod, authException);
	}

}
