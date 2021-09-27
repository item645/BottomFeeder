package io.bottomfeeder.data;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.filter.EntryFilterService;
import io.bottomfeeder.filter.model.DigestEntryFilterData;
import io.bottomfeeder.filter.model.SourceFeedEntryFilterData;
import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.sourcefeed.SourceFeedService;
import io.bottomfeeder.user.User;
import io.bottomfeeder.user.UserService;

/**
 * A service providing functionality for exporting application data into JSON.
 */
@Service
public class DataExportService {

	private final UserService userService;
	private final DigestService digestService;
	private final SourceFeedService sourceFeedService;
	private final EntryFilterService entryFilterService;
	private final ObjectMapper objectMapper;
	
	
	/**
	 * Provides a mix-in annotation to ignore the id property of serializable data object.
	 */
	private interface IgnoreIdMixin {
		@JsonIgnore Long id();
	}
	
	
	public DataExportService(
			UserService userService, 
			DigestService digestService, 
			SourceFeedService sourceFeedService,
			EntryFilterService entryFilterService) {
		this.userService = userService;
		this.digestService = digestService;
		this.sourceFeedService = sourceFeedService;
		this.entryFilterService = entryFilterService;
		
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		// Exclude filter ids fron serialization
		objectMapper.addMixIn(DigestEntryFilterData.class, IgnoreIdMixin.class);
		objectMapper.addMixIn(SourceFeedEntryFilterData.class, IgnoreIdMixin.class);
	}


	public byte[] exportUsersData() {
		return serialize(collectUsersData(), Data.TYPE_USERS);
	}

	
	public byte[] exportDigestsData(User owner) {
		return serialize(collectDigestsData(Objects.requireNonNull(owner)), Data.TYPE_DIGESTS);
	}
	
	
	public byte[] exportSourceFeedsData(Digest digest) {
		return serialize(collectSourceFeedsData(Objects.requireNonNull(digest)), Data.TYPE_SOURCE_FEEDS);
	}
	
	
	private byte[] serialize(Collection<?> dataItems, String dataType) {
		assert dataItems != null;
		assert StringUtils.equalsAny(dataType, Data.TYPE_USERS, Data.TYPE_DIGESTS, Data.TYPE_SOURCE_FEEDS);
		
		try {
			return objectMapper.writeValueAsBytes(new Data<>(dataItems, dataType));
		}
		catch (JsonProcessingException e) {
			throw new DataExportException(e);
		}
	}
	
	
	private List<UserData> collectUsersData() {
		return userService.getAllUsers().stream().map(this::createUserData).collect(toList());
	}
	
	
	private UserData createUserData(User user) {
		return new UserData(user, collectDigestsData(user));
	}


	private List<DigestData> collectDigestsData(User owner) {
		return digestService.getOwnerDigests(owner).stream().map(this::createDigestData).collect(toList());
	}
	
	
	private DigestData createDigestData(Digest digest) {
		return new DigestData(digest, collectSourceFeedsData(digest), collectDigestEntryFiltersData(digest));
	}


	private List<SourceFeedData> collectSourceFeedsData(Digest digest) {
		return sourceFeedService.getDigestSourceFeeds(digest).stream().map(this::createSourceFeedData).collect(toList());
	}

	
	private SourceFeedData createSourceFeedData(SourceFeed sourceFeed) {
		return new SourceFeedData(sourceFeed, collectSourceFeedEntryFiltersData(sourceFeed));
	}
	
	
	private List<DigestEntryFilterData> collectDigestEntryFiltersData(Digest digest) {
		return entryFilterService.getDigestEntryFilters(digest).stream()
				.map(DigestEntryFilterData::new)
				.collect(toList());
	}
	
	
	private List<SourceFeedEntryFilterData> collectSourceFeedEntryFiltersData(SourceFeed sourceFeed) {
		return entryFilterService.getSourceFeedEntryFilters(sourceFeed).stream()
				.map(SourceFeedEntryFilterData::new)
				.collect(toList());
	}
	
}
