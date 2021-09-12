package io.bottomfeeder.sourcefeed.entry;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FastByteArrayOutputStream;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.feed.DigestFeedFormat;
import io.bottomfeeder.filter.EntryFilterService;
import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * A service providing functionality for working with source feed's content represented
 * as {@code SourceFeedEntry} instances.
 */
@Service
public class SourceFeedEntryService {

	private static final Logger logger = LoggerFactory.getLogger(SourceFeedEntryService.class);
	
	private final SourceFeedEntryRepository sourceFeedEntryRepository;
	private final EntryFilterService entryFilterService;
	private final SyndFeedInput syndFeedInput = new SyndFeedInput();
	private final SyndFeedOutput syndFeedOutput = new SyndFeedOutput();

	
	public SourceFeedEntryService(
			SourceFeedEntryRepository sourceFeedEntryRepository, 
			EntryFilterService entryFilterService) {
		this.sourceFeedEntryRepository = sourceFeedEntryRepository;
		this.entryFilterService = entryFilterService;
	}

	
	@Transactional
	public List<SyndEntry> loadDigestFeedContent(Digest digest, DigestFeedFormat targetFormat) {
		var entryFilterChain = entryFilterService.getDigestEntryFilterChain(digest);
		if (entryFilterChain == null) {
			return sourceFeedEntryRepository.findDigestFeedEntries(digest).stream()
					.map(sourceFeedEntry -> readSourceFeedEntryContent(sourceFeedEntry, targetFormat))
					.filter(Objects::nonNull)
					.collect(toList());
		}
		else {
			return sourceFeedEntryRepository.findDigestFeedEntries(digest, Pageable.unpaged()).stream()
					.map(sourceFeedEntry -> readSourceFeedEntryContent(sourceFeedEntry, targetFormat))
					.filter(entryFilterChain)
					.limit(digest.getMaxEntries())
					.collect(toList());
		}
		
	}
	
	
	@Transactional
	public void replaceSourceFeedEntries(SyndFeed newFeedData, SourceFeed sourceFeed) {
		Objects.requireNonNull(newFeedData);
		Objects.requireNonNull(sourceFeed.getId());
		
		deleteSourceFeedEntries(sourceFeed);
		
		var syndEntryStream = newFeedData.getEntries().stream();
		
		var entryFilterChain = entryFilterService.getSourceFeedEntryFilterChain(sourceFeed);
		if (entryFilterChain != null)
			syndEntryStream = syndEntryStream.filter(entryFilterChain);
		
		var maxEntries = sourceFeed.getMaxEntries();
		if (maxEntries > 0)
			syndEntryStream = syndEntryStream.limit(maxEntries);
		
		var feedType = newFeedData.getFeedType();
		var sourceFeedEntries = syndEntryStream
				.map(syndEntry -> createSourceFeedEntry(syndEntry, feedType, sourceFeed))
				.filter(Objects::nonNull) // filter out entries without published and updated date
				.collect(toList());
		
		sourceFeedEntryRepository.saveAll(sourceFeedEntries);
	}


	public void deleteSourceFeedEntries(SourceFeed sourceFeed) {
		deleteSourceFeedEntries(sourceFeed.getId());
	}
	
	
	public void deleteSourceFeedEntries(long sourceFeedId) {
		sourceFeedEntryRepository.deleteBySourceFeedId(sourceFeedId);
	}
	
	
	private SyndEntry readSourceFeedEntryContent(SourceFeedEntry sourceFeedEntry, DigestFeedFormat targetFormat) {
		try (var input = new ByteArrayInputStream(sourceFeedEntry.getContent())) {
			var syndEntry = syndFeedInput.build(new XmlReader(input)).getEntries().get(0);
			fixEntryDate(syndEntry, sourceFeedEntry, targetFormat);
			return syndEntry;
		}
		catch (IOException | IllegalArgumentException | FeedException e) {
			logger.error(format("Failed to read content of source feed entry %d", sourceFeedEntry.getId()), e);
			return null;
		}
	}
	
	
	private static void fixEntryDate(SyndEntry syndEntry, SourceFeedEntry sourceFeedEntry, 
			DigestFeedFormat targetFormat) {
		var pubDate = syndEntry.getPublishedDate();
		if (pubDate == null) {
			var updatedDate = syndEntry.getUpdatedDate();
			if (updatedDate == null) {
				// Should not happen as we have filtered out all such entries on content save
				var message = format("Invalid content of source feed entry %d: missing published and updated date",
						sourceFeedEntry.getId());
				throw new SourceFeedEntryException(message);
			}
			else if (targetFormat == DigestFeedFormat.RSS_2_0) {
				// During Atom to RSS conversion updated date won't be serialized because RSS format
				// doesn't support this field for entries. So, if pub date is absent too, we will 
				// have to use updated date as pub date instead.
				syndEntry.setPublishedDate(updatedDate);
			}
		}
	}
	
	
	private SourceFeedEntry createSourceFeedEntry(SyndEntry syndEntry, String feedType, SourceFeed sourceFeed) {
		var date = getEntryDate(syndEntry);
		if (date != null) {
			var entryFeed = createEntryFeed(syndEntry, feedType);
			return new SourceFeedEntry(date, getContentBytes(entryFeed), sourceFeed);
		}
		else {
			var message = format("Could not create source feed entry for SyndEntry instance "
					+ "of source feed %s because it contains neither published nor updated date",
					sourceFeed.getSource());
			logger.warn(message);
			return null;
		}
	}
	
	
	private static SyndFeed createEntryFeed(SyndEntry syndEntry, String feedType) {
		// Dummy feed for single entry
		var entryFeed = new SyndFeedImpl();
		entryFeed.setFeedType(feedType);
		entryFeed.setTitle("");
		entryFeed.setDescription("");
		entryFeed.setLink("");
		entryFeed.setEntries(List.of(syndEntry));
		return entryFeed;
	}
	
	
	private byte[] getContentBytes(SyndFeed entryFeed) {
		var out = new FastByteArrayOutputStream();
		try (var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			syndFeedOutput.output(entryFeed, writer);
		}
		catch (IOException | FeedException e) {
			throw new SourceFeedEntryException(e);
		}
		return out.toByteArrayUnsafe();
	}

	
	private static Instant getEntryDate(SyndEntry syndEntry) {
		var date = syndEntry.getPublishedDate();
		if (date == null) {
			date = syndEntry.getUpdatedDate();
			if (date == null)
				return null;
		}
		return date.toInstant();
	}

}
