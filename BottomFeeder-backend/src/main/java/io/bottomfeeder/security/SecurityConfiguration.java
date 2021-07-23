package io.bottomfeeder.security;

import static io.bottomfeeder.config.Constants.ANONYMOUS_PRINCIPAL;
import static io.bottomfeeder.config.Constants.API_URL_BASE;
import static io.bottomfeeder.config.Constants.DIGEST_FEED_URL;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessEventPublishingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bottomfeeder.api.model.ErrorResponse;
import io.bottomfeeder.api.model.Response;

/**
 * Configures security for the application.
 */
@EnableWebSecurity
class SecurityConfiguration {
	
	/**
	 * General web security settings.
	 */
	@Configuration
	class GeneralSecurityConfiguration extends WebSecurityConfigurerAdapter {

		private final UserDetailsService userDetailsService;
		private final AuthenticationEntryPoint authenticationEntryPoint;
		private final AccessDeniedHandler accessDeniedHandler;
		private final Environment environment;
		
		public GeneralSecurityConfiguration(
				UserDetailsService userDetailsService,
				AuthenticationEntryPoint authenticationEntryPoint, 
				AccessDeniedHandler accessDeniedHandler, 
				Environment environment) {
			this.userDetailsService = userDetailsService;
			this.authenticationEntryPoint = authenticationEntryPoint;
			this.accessDeniedHandler = accessDeniedHandler;
			this.environment = environment;
		}

		
		private boolean isDevelopmentMode() {
			return Arrays.asList(environment.getActiveProfiles()).contains("dev");
		}
		
		
		@Override
		public void configure(WebSecurity webSecurity) throws Exception {
			webSecurity
				.ignoring()
				.antMatchers("/*.{js,html,css}")
				.and()
			.debug(isDevelopmentMode()); // TODO set this through a property
		}
		

		@Override
		protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
			authenticationManagerBuilder
				.userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder());
		}
		
		
		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
			.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
				.and()
			.anonymous()
				.principal(ANONYMOUS_PRINCIPAL)
				.and()
			.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers(DIGEST_FEED_URL + "/**").permitAll()
				.antMatchers(API_URL_BASE + "/authenticate").anonymous()
				.antMatchers(API_URL_BASE + "/signup").anonymous()
				.anyRequest().authenticated()
				.and()
			.cors()
				.and()
			.csrf()			
				.disable()
			.logout()
				.disable()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.sessionAuthenticationStrategy(concurrentSessionAuthStrategy())
				.maximumSessions(-1)
				.expiredSessionStrategy(expiredSessionStrategy())
				.sessionRegistry(sessionRegistry());
		}
		
		
		@Override
		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}
	}
	
	
	/**
	 * Extends general web security configuration to add the ability to access digest
	 * feed using HTTP Basic authentication.
	 * This is necessary to support accessing private digests.
	 */
	@Configuration
	@Order(1)
	class DigestFeedSecurityConfiguration extends GeneralSecurityConfiguration {

		public DigestFeedSecurityConfiguration(
				UserDetailsService userDetailsService,
				AuthenticationEntryPoint authenticationEntryPoint, 
				AccessDeniedHandler accessDeniedHandler,
				Environment environment) {
			super(userDetailsService, authenticationEntryPoint, accessDeniedHandler, environment);
		}

		
		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			super.configure(httpSecurity);
			httpSecurity.antMatcher(DIGEST_FEED_URL + "/**").httpBasic();
		}

	}
	
	
	/**
	 * Implements action taken when expired session is detected.
	 * This implementation sends a JSON-converted error response with related details.
	 */
	private static class ErrorResponseExpiredSessionStrategy implements SessionInformationExpiredStrategy {
		
		@Override
		public void onExpiredSessionDetected(SessionInformationExpiredEvent event) 
				throws IOException, ServletException {
			var httpStatus = HttpStatus.UNAUTHORIZED;
			var message = String.format("Error: %s", httpStatus.getReasonPhrase());
			var detail = String.format("Session ID: %s", event.getSessionInformation().getSessionId());
			var error = new ErrorResponse("Session has expired", detail);
			var response = new Response<>(httpStatus.value(), message, error);
			
			var servletResponse = event.getResponse();
			servletResponse.setStatus(httpStatus.value());
			servletResponse.setCharacterEncoding("UTF-8");
			servletResponse.setHeader("Content-Type", "application/json");
			servletResponse.getWriter().write(new ObjectMapper().writeValueAsString(response));
			servletResponse.flushBuffer();
		}
	}
	
	
	@Bean
	public LogoutHandler logoutHandler() {
		return new CompositeLogoutHandler(
				new CookieClearingLogoutHandler("JSESSIONID"),
				new SecurityContextLogoutHandler(),
				new LogoutSuccessEventPublishingLogoutHandler());
	}
	

	// TODO make CORS configurable, should be disabled in production
	@Bean
	public CorsFilter corsFilter() {
		var all = List.of("*");
		var corsCfg = new CorsConfiguration();
		corsCfg.setAllowedOriginPatterns(all);
		corsCfg.setAllowedMethods(all);
		corsCfg.setAllowedHeaders(all);
		corsCfg.setAllowCredentials(true);
		corsCfg.setMaxAge(1800L);
		
		var cfgSource = new UrlBasedCorsConfigurationSource();
		cfgSource.registerCorsConfiguration(API_URL_BASE + "/**", corsCfg);
		
		return new CorsFilter(cfgSource);
	}
	
	
	@Bean
	public GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults("");
	}
	
	
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	
	@Bean
	public SessionInformationExpiredStrategy expiredSessionStrategy() {
		return new ErrorResponseExpiredSessionStrategy();
	}
	
	
	@Bean
	public SessionAuthenticationStrategy concurrentSessionAuthStrategy() {
		var controlStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
		controlStrategy.setMaximumSessions(-1);
		return new CompositeSessionAuthenticationStrategy(List.of(
				controlStrategy,
				new SessionFixationProtectionStrategy(),
				new RegisterSessionAuthenticationStrategy(sessionRegistry())));
	}

	
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}

