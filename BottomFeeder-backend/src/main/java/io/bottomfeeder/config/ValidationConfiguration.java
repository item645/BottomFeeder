package io.bottomfeeder.config;

import javax.validation.Validator;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configures validation settings, including validator and message source.
 */
@Configuration
class ValidationConfiguration {

	@Bean
	public MessageSource messageSource() {
		var messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	
	@Bean
	public Validator validator() {
		var validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(messageSource());
		return validator;
	}


	@Bean
	public HibernatePropertiesCustomizer hibernateValidationCustomizer() {
		return hibernateProperties -> hibernateProperties.put("javax.persistence.validation.factory", validator());
	}
	
}
