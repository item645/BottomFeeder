package io.bottomfeeder.data;

import static io.bottomfeeder.user.User.PASSWORD_MIN_SIZE;
import static io.bottomfeeder.user.User.PASSWORD_HASH_REGEX;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_SIZE;
import static io.bottomfeeder.user.User.VALIDATION_PASSWORD_HASH_REGEX;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
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
import io.bottomfeeder.filter.EntryFilterService;
import io.bottomfeeder.filter.model.DigestEntryFilterData;
import io.bottomfeeder.filter.model.DigestEntryFilterList;
import io.bottomfeeder.filter.model.SourceFeedEntryFilterData;
import io.bottomfeeder.filter.model.SourceFeedEntryFilterList;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.sourcefeed.SourceFeedService;
import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserService;
import io.bottomfeeder.util.TransactionalRunner;

/**
 * A service providing functionality for importing data represented in JSON 
 * format into application.
 */
@Service
@Transactional
public class DataImportService {

	private static final Pattern BCRYPT_HASH_PATTERN = Pattern.compile(PASSWORD_HASH_REGEX);
	
	private static final TypeReference<Data<UserData>> USERS_DATA_TYPE_REF = new TypeReference<>(){};
	private static final TypeReference<Data<DigestData>> DIGESTS_DATA_TYPE_REF = new TypeReference<>(){};
	private static final TypeReference<Data<SourceFeedData>> SOURCE_FEEDS_DATA_TYPE_REF = new TypeReference<>(){};
	
	private final UserService userService;
	private final DigestService digestService;
	private final SourceFeedService sourceFeedService;
	private final EntryFilterService entryFilterService;
	private final ObjectMapper objectMapper;
	private final MessageSource messageSource;
	private final TransactionalRunner transactionalRunner;
	
	
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
			EntryFilterService entryFilterService,
			Validator validator, 
			MessageSource messageSource,
			TransactionalRunner transactionalRunner) {
		this.userService = userService;
		this.digestService = digestService;
		this.sourceFeedService = sourceFeedService;
		this.entryFilterService = entryFilterService;
		this.objectMapper = createObjectMapper(validator);
		this.messageSource = messageSource;
		this.transactionalRunner = transactionalRunner;
	}
	
	
	// TODO better error reporting, should clearly indicate the deserialized object where error occured
	
	void importInitialData(byte[] initialDataJson) {
		transactionalRunner.run(() -> {
			assert !userService.hasUsers() : "Database already contains data";
			
			importData(initialDataJson, USERS_DATA_TYPE_REF, 
					compose(this::ensureContainsAdminAccount, this::importUsersData));
		});
	}
	
	
	public void importUsers(byte[] usersDataJson) {
		importData(usersDataJson, USERS_DATA_TYPE_REF, this::importUsersData);
	}
	
	
	public void importDigests(byte[] digestsDataJson, User owner) {
		Objects.requireNonNull(owner.getId());
		importData(digestsDataJson, DIGESTS_DATA_TYPE_REF, data -> importDigestsData(data, owner));
	}
	
	
	public void importSourceFeeds(byte[] sourceFeedsDataJson, Digest digest) {
		Objects.requireNonNull(digest.getId());
		importData(sourceFeedsDataJson, SOURCE_FEEDS_DATA_TYPE_REF, data -> importSourceFeedsData(data, digest));
	}
	
	
	private <T> void importData(byte[] dataJson, TypeReference<Data<T>> typeRef, Consumer<Data<T>> dataConsumer) {
		try {
			dataConsumer.accept(objectMapper.readValue(dataJson, typeRef));
		}
		catch (DataImportException e) {
			throw e;
		}
		catch (Exception e) {
			throw new DataImportException("The data is probably not valid", e);
		}
	}

	
	private void importUsersData(Data<UserData> data) {
		data.items().forEach(this::saveUserData);
	}
	
	
	private void importDigestsData(Data<DigestData> data, User owner) {
		data.items().forEach(digestData -> saveDigestData(digestData, owner));
	}
	
	
	private void importSourceFeedsData(Data<SourceFeedData> data, Digest digest) {
		data.items().forEach(sourceFeedData -> saveSourceFeedData(sourceFeedData, digest));
	}
	
	
	private void ensureContainsAdminAccount(Data<UserData> usersData) {
		if (!usersData.items().stream().anyMatch(userData -> userData.role() == Role.ADMIN))
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
		var digest = digestService.createDigest(owner, digestData.title(), digestData.maxEntries(), 
				digestData.isPrivate(), digestData.externalId());
		
		digestData.sourceFeeds().forEach(sourceFeedData -> saveSourceFeedData(sourceFeedData, digest));
		saveDigestEntryFiltersData(digestData.entryFilters(), digest);
	}
	
	
	private void saveSourceFeedData(SourceFeedData sourceFeedData, Digest digest) {
		var sourceFeed = sourceFeedService.createSourceFeed(digest, sourceFeedData.source(), 
				sourceFeedData.contentUpdateInterval(), sourceFeedData.maxEntries(), false);
		saveSourceFeedEntryFiltersData(sourceFeedData.entryFilters(), sourceFeed);
	}
	
	
	private void saveDigestEntryFiltersData(List<DigestEntryFilterData> entryFilters, Digest digest) {
		if (!entryFilters.isEmpty())
			entryFilterService.updateDigestEntryFilters(new DigestEntryFilterList(entryFilters), digest);
	}
	
	
	private void saveSourceFeedEntryFiltersData(List<SourceFeedEntryFilterData> entryFilters, SourceFeed sourceFeed) {
		if (!entryFilters.isEmpty())
			entryFilterService.updateSourceFeedEntryFilters(new SourceFeedEntryFilterList(entryFilters), sourceFeed);
	}
	
	
	private static String unwrapPlaceholder(String message) {
		if (message.startsWith("{") && message.endsWith("}"))
			return StringUtils.removeStart(StringUtils.removeEnd(message, "}"), "{");
		return message;
	}

	
	private static <T> Consumer<T> compose(Consumer<T> first, Consumer<T> second) {
		return first.andThen(second);
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
