package io.bottomfeeder.security.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Default access denied handler implementation.
 */
@Component
class DefaultAccessDeniedHandler extends HandlerBase<AccessDeniedException> implements AccessDeniedHandler {

	DefaultAccessDeniedHandler(
			ApplicationContext applicationContext, 
			HandlerExceptionResolver handlerExceptionResolver) {
		super(applicationContext, handlerExceptionResolver);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		var handlerMethod = getHandlerMethod("handleAccessDeniedError", AccessDeniedException.class);
		handlerExceptionResolver.resolveException(request, response, handlerMethod, accessDeniedException);
	}

}
