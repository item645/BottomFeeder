package io.bottomfeeder.digest;

import static java.lang.String.format;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

import io.bottomfeeder.digest.feed.DigestFeedFormat;
import io.bottomfeeder.sourcefeed.SourceFeedService;
import io.bottomfeeder.user.User;

/**
 * A service providing common functionality for working with digests.
 */
@Service
public class DigestService {

	private static final Comparator<SyndEntry> ENTRY_DATE_DESC = new FeedEntryDateComparator();
	
	private final DigestRepository digestRepository;
	private final SourceFeedService sourceFeedService;
	private final String applicationName;
	private final String applicationUrl;
	
	
	/**
	 * A comparator that compares feed entries by either published or updated date
	 * (whichever is present), imposing a descending order on them.
	 */
	private static class FeedEntryDateComparator implements Comparator<SyndEntry> {

		@Override
		public int compare(SyndEntry entry1, SyndEntry entry2) {
			return getDate(entry2).compareTo(getDate(entry1));
		}
		
		private static Date getDate(SyndEntry entry) {
			var date = entry.getPublishedDate();
			if (date == null) {
				date = entry.getUpdatedDate();
				if (date == null)
					throw new IllegalArgumentException("SyndEntry should contain either published date"
							+ " or updated date");
			}
			return date;
		}
	}
	
	
	public DigestService(
			DigestRepository digestRepository, 
			SourceFeedService sourceFeedService,
			@Value("${bf.application.name}") String applicationName,
			@Value("${bf.application.url}") String applicationUrl) {
		this.digestRepository = digestRepository;
		this.sourceFeedService = sourceFeedService;
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
	
	
	public Digest createDigest(User owner, String title, int maxItems, boolean isPrivate) {
		return saveNewDigest(owner, title, maxItems, isPrivate, createUnusedExternalId(), false);
	}
	
	
	public Digest createDigest(User owner, String title, int maxItems, boolean isPrivate, String externalId) {
		return saveNewDigest(owner, title, maxItems, isPrivate, externalId, true);
	}
	
	
	private Digest saveNewDigest(User owner, String title, int maxItems, boolean isPrivate, 
			String externalId, boolean checkExternalIdDuplicate) {
		if (digestRepository.existsByTitleIgnoreCaseAndOwner(title, owner))
			throw duplicateDigestError(title, owner);
		if (checkExternalIdDuplicate && digestRepository.existsByExternalId(externalId))
			throw new DigestException(format("Duplicate digest external ID: %s", externalId));
		
		return digestRepository.save(new Digest(title, maxItems, isPrivate, owner, externalId));
	}
	
	
	private String createUnusedExternalId() {
		String id;
		do
			id = Digest.createExternalId();
		while (digestRepository.existsByExternalId(id));
		return id;
	}
	
	
	public Digest updateDigest(long id, String newTitle, int newMaxItems, boolean newIsPrivate) {
		var digest = getDigest(id);
		var owner = digest.getOwner(); 
		if (!digest.getTitle().equalsIgnoreCase(newTitle) 
				&& digestRepository.existsByTitleIgnoreCaseAndOwner(newTitle, owner))
			throw duplicateDigestError(newTitle, owner);
		
		digest.setTitle(newTitle);
		digest.setMaxItems(newMaxItems);
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
		
		// TODO measure and optimize this sequence for performance (if needed)
		var entries = sourceFeedService.loadDigestSourceFeedsContent(digest).stream()
				.map(entry -> fixEntryDate(entry, digestFeedFormat))
				.filter(Objects::nonNull) // filter out entries without published/updated date
				.sorted(ENTRY_DATE_DESC)
				.limit(digest.getMaxItems())
				.collect(Collectors.toList());
		
		digestFeed.setEntries(entries);
		return digestFeed;
	}
	
	
	private Digest getDigest(String externalId) {
		return digestRepository.findOneByExternalId(externalId).orElseThrow(
				() -> new DigestException(format("Digest with id '%s' not found", externalId)));
	}

	
	private static SyndEntry fixEntryDate(SyndEntry entry, DigestFeedFormat targetFormat) {
		var pubDate = entry.getPublishedDate();
		if (pubDate == null) {
			var updatedDate = entry.getUpdatedDate();
			if (updatedDate == null)
				return null;
			else if (targetFormat == DigestFeedFormat.RSS_2_0)
				// During Atom to RSS conversion updated date won't be serialized because RSS format
				// doesn't support this field for entries. So, if pub date is absent too, we will 
				// have to use updated date as pub date instead.
				entry.setPublishedDate(updatedDate);
		}
		return entry;
	}
	
}
