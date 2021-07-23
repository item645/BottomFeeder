package io.bottomfeeder.data;

import static io.bottomfeeder.user.User.PASSWORD_MIN_SIZE;
import static io.bottomfeeder.user.User.PASSWORD_HASH_REGEX;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_SIZE;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_HASH_REGEX;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.sourcefeed.SourceFeedService;
import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserService;

/**
 * A service providing functionality for importing data represented in JSON 
 * format into application.
 */
@Service
public class DataImportService {

	private static final Pattern BCRYPT_HASH_PATTERN = Pattern.compile(PASSWORD_HASH_REGEX);
	
	private static final TypeReference<Data<UserData>> USERDATA_TYPE_REFERENCE = 
			new TypeReference<Data<UserData>>(){};
	
	private final UserService userService;
	private final DigestService digestService;
	private final SourceFeedService sourceFeedService;
	private final ObjectMapper objectMapper;
	private final MessageSource messageSource;
	
	
	/**
	 * Custom deserializer that performs JSR 380 validation on deserialized objects.
	 */
	@SuppressWarnings("serial")
	private static class ValidatingDeserializer extends BeanDeserializer {

		private final Validator validator;
		
		ValidatingDeserializer(BeanDeserializerBase src, Validator validator) {
			super(src);
			this.validator = validator;
		}

		@Override
		public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			var object = super.deserialize(parser, context);
			validate(object);
			return object;
		}
		
		private void validate(Object object) {
			var violations = validator.validate(object);
			if (!violations.isEmpty())
				throw new ConstraintViolationException(violations);
		}
	}
	
	
	public DataImportService(
			UserService userService,
			DigestService digestService, 
			SourceFeedService sourceFeedService,
			Validator validator, 
			MessageSource messageSource) {
		this.userService = userService;
		this.digestService = digestService;
		this.sourceFeedService = sourceFeedService;
		this.objectMapper = createObjectMapper(validator);
		this.messageSource = messageSource;
	}
	
	
	@Transactional
	public void importUsersData(String usersDataJson) {
		// TODO better error reporting, should clearly indicate the deserialized object where error occured
		try {
			var importedData = objectMapper.readValue(usersDataJson, USERDATA_TYPE_REFERENCE);
			
			if (!userService.hasUsers())
				ensureContainsAdminAccount(importedData.items());
			
			importedData.items().forEach(this::saveUserData);
		}
		catch (DataImportException e) {
			throw e;
		}
		catch (Exception e) {
			throw new DataImportException("Error importing data", e);
		}
	}

	
	private static void ensureContainsAdminAccount(Collection<UserData> users) {
		if (!users.stream().anyMatch(userData -> userData.role() == Role.ADMIN))
			throw new DataImportException("Users data must contain at least one user with ADMIN role");
	}
	
	
	private void saveUserData(UserData userData) {
		var passwordData = validatePasswordData(userData.password());
		var hashPassword = passwordData.format() == PasswordFormat.PLAIN_TEXT;
		
		var user = userService.createUser(userData.login(), passwordData.value(), hashPassword, userData.role());
		userData.digests().forEach(digestData -> saveDigestData(digestData, user));
	}
	
	
	private PasswordData validatePasswordData(PasswordData passwordData) {
		switch (passwordData.format()) {
			case PLAIN_TEXT -> {
				if (passwordData.value().length() < PASSWORD_MIN_SIZE) {
					var message = messageSource.getMessage(unwrapPlaceholder(VALIDATION_PASSWORD_SIZE), 
							null, Locale.ROOT);
					message = StringUtils.replace(message, "{min}", Integer.toString(PASSWORD_MIN_SIZE));
					throw new DataImportException(message);
				}
			}
			case BCRYPT_HASH -> {
				if (!BCRYPT_HASH_PATTERN.matcher(passwordData.value()).matches()) {
					var message = messageSource.getMessage(unwrapPlaceholder(VALIDATION_PASSWORD_HASH_REGEX),
							null, Locale.ROOT);
					throw new DataImportException(message);
				}
			}
			default -> throw new IllegalArgumentException("Unsupported password format: " + passwordData.format());
		}
		return passwordData;
	}
	
	
	private void saveDigestData(DigestData digestData, User owner) {
		var digest = digestService.createDigest(owner, digestData.title(), digestData.maxItems(), 
				digestData.isPrivate(), digestData.externalId());
		digestData.sourceFeeds().forEach(sourceFeedData -> saveSourceFeedData(sourceFeedData, digest));
	}
	
	
	private void saveSourceFeedData(SourceFeedData sourceFeedData, Digest digest) {
		sourceFeedService.createSourceFeed(digest, sourceFeedData.source(), 
				sourceFeedData.contentUpdateInterval(), false);
	}
	
	
	private static String unwrapPlaceholder(String message) {
		if (message.startsWith("{") && message.endsWith("}"))
			return StringUtils.removeStart(StringUtils.removeEnd(message, "}"), "{");
		return message;
	}
	
	
	private static ObjectMapper createObjectMapper(Validator validator) {
		var objectMapper = new ObjectMapper();
		
		var module = new SimpleModule();
		module.setDeserializerModifier(new BeanDeserializerModifier() {
			@Override
			public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
					JsonDeserializer<?> deserializer) {
				if (deserializer instanceof BeanDeserializer beanDeserializer)
					return new ValidatingDeserializer(beanDeserializer, validator);
				return deserializer;
			}
		});
		objectMapper.registerModule(module);
		
		return objectMapper;
	}

}
