package io.bottomfeeder.digest;

import static java.lang.String.format;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

import io.bottomfeeder.digest.feed.DigestFeedFormat;
import io.bottomfeeder.filter.EntryFilterService;
import io.bottomfeeder.sourcefeed.SourceFeedService;
import io.bottomfeeder.sourcefeed.entry.SourceFeedEntryService;
import io.bottomfeeder.user.User;

/**
 * A service providing common functionality for working with digests.
 */
@Service
public class DigestService {

	private final DigestRepository digestRepository;
	private final SourceFeedService sourceFeedService;
	private final SourceFeedEntryService sourceFeedEntryService;
	private final EntryFilterService entryFilterService;
	private final String applicationName;
	private final String applicationUrl;
	
	
	public DigestService(
			DigestRepository digestRepository, 
			SourceFeedService sourceFeedService,
			SourceFeedEntryService sourceFeedEntryService,
			EntryFilterService entryFilterService,
			@Value("${bf.application.name}") String applicationName,
			@Value("${bf.application.url}") String applicationUrl) {
		this.digestRepository = digestRepository;
		this.sourceFeedService = sourceFeedService;
		this.sourceFeedEntryService = sourceFeedEntryService;
		this.entryFilterService = entryFilterService;
		this.applicationName = checkPropertyValue(applicationName, "Application name").trim();
		this.applicationUrl = checkPropertyValue(applicationUrl, "Application URL").trim();
	}
	
	
	private static String checkPropertyValue(String propertyValue, String propertyDescription) {
		if (StringUtils.isBlank(propertyValue))
			throw new IllegalArgumentException(format("%s not specified", propertyDescription));
		return propertyValue;
	}
	
	
	public Digest getDigest(long id) {
		return digestRepository.findById(id).orElseThrow(
				() -> new DigestException(format("Digest with id '%d' not found", id)));
	}

	
	public List<Digest> getAllDigests() {
		return digestRepository.findAll(Sort.by("creationDate").descending());
	}
	
	
	public List<Digest> getOwnerDigests(User owner) {
		return digestRepository.findByOwnerOrderByCreationDateDesc(owner);
	}
	
	
	public Digest createDigest(User owner, String title, int maxEntries, boolean isPrivate) {
		return saveNewDigest(owner, title, maxEntries, isPrivate, createUnusedExternalId(), false);
	}
	
	
	public Digest createDigest(User owner, String title, int maxEntries, boolean isPrivate, String externalId) {
		return saveNewDigest(owner, title, maxEntries, isPrivate, externalId, true);
	}
	
	
	private Digest saveNewDigest(User owner, String title, int maxEntries, boolean isPrivate, 
			String externalId, boolean checkExternalIdDuplicate) {
		if (digestRepository.existsByTitleIgnoreCaseAndOwner(title, owner))
			throw duplicateDigestError(title, owner);
		if (checkExternalIdDuplicate && digestRepository.existsByExternalId(externalId))
			throw new DigestException(format("Duplicate digest external ID: %s", externalId));
		
		return digestRepository.save(new Digest(title, maxEntries, isPrivate, owner, externalId));
	}
	
	
	private String createUnusedExternalId() {
		String id;
		do
			id = Digest.createExternalId();
		while (digestRepository.existsByExternalId(id));
		return id;
	}
	
	
	public Digest updateDigest(long id, String newTitle, int newMaxEntries, boolean newIsPrivate) {
		var digest = getDigest(id);
		var owner = digest.getOwner(); 
		if (!digest.getTitle().equalsIgnoreCase(newTitle) 
				&& digestRepository.existsByTitleIgnoreCaseAndOwner(newTitle, owner))
			throw duplicateDigestError(newTitle, owner);
		
		digest.setTitle(newTitle);
		digest.setMaxEntries(newMaxEntries);
		digest.setPrivate(newIsPrivate);
		
		return digestRepository.save(digest);
	}
	

	private static DigestException duplicateDigestError(String title, User owner) {
		return new DigestException(format("Digest with title '%s' already exists for user '%s'",
				title, owner.getLogin()));
	}
	
	
	@Transactional
	public void deleteDigest(long id) {
		deleteDigest(getDigest(id));
	}

	
	private void deleteDigest(Digest digest) {
		sourceFeedService.deleteDigestSourceFeeds(digest);
		entryFilterService.deleteDigestEntryFilters(digest.getId());
		digestRepository.delete(digest);
	}
	
	
	@Transactional
	public void deleteUserDigests(User user) {
		digestRepository.findByOwner(user).stream().forEach(this::deleteDigest);
	}

	
	public String getDigestFeedLink(String digestExternalId, DigestFeedFormat digestFeedFormat) {
		return format("%s/digest/%s/feed.%s", applicationUrl, digestExternalId, digestFeedFormat.extension());
	}
	
	
	public SyndFeed getDigestFeed(String externalId, DigestFeedFormat digestFeedFormat) {
		var digest = getDigest(externalId);
		
		var digestFeed = new SyndFeedImpl();
		digestFeed.setFeedType(digestFeedFormat.type());
		digestFeed.setEncoding("UTF-8");
		digestFeed.setTitle(digest.getTitle());
		digestFeed.setDescription(digest.getTitle());
		digestFeed.setGenerator(applicationName);
		digestFeed.setLink(getDigestFeedLink(externalId, digestFeedFormat));
		digestFeed.setEntries(sourceFeedEntryService.loadDigestFeedContent(digest, digestFeedFormat));
		
		return digestFeed;
	}
	
	
	private Digest getDigest(String externalId) {
		return digestRepository.findOneByExternalId(externalId).orElseThrow(
				() -> new DigestException(format("Digest with id '%s' not found", externalId)));
	}
	
}
