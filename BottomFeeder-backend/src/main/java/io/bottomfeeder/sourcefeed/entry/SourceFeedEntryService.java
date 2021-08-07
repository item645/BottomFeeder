package io.bottomfeeder.sourcefeed.entry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * A service providing functionality for working with source feed's content represented
 * as {@code SourceFeedEntry} instances.
 */
@Service
public class SourceFeedEntryService {

	private static final Logger logger = LoggerFactory.getLogger(SourceFeedEntryService.class);
	
	private final SourceFeedEntryRepository sourceFeedEntryRepository;
	private final SyndFeedInput syndFeedInput = new SyndFeedInput();
	private final SyndFeedOutput syndFeedOutput = new SyndFeedOutput();

	
	public SourceFeedEntryService(SourceFeedEntryRepository sourceFeedEntryRepository) {
		this.sourceFeedEntryRepository = sourceFeedEntryRepository;
	}

	
	@Transactional
	public List<SyndEntry> loadSourceFeedContent(SourceFeed sourceFeed) {
		return sourceFeedEntryRepository.findBySourceFeed(sourceFeed).stream()
				.map(this::readSourceFeedEntryContent)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
	}

	
	@Transactional
	public void replaceSourceFeedEntries(SyndFeed newFeedData, SourceFeed sourceFeed) {
		Objects.requireNonNull(newFeedData);
		Objects.requireNonNull(sourceFeed.getId());
		
		deleteSourceFeedEntries(sourceFeed);
		
		var feedType = newFeedData.getFeedType();
		var sourceFeedEntries = newFeedData.getEntries().stream()
				.map(syndEntry -> createSourceFeedEntry(syndEntry, feedType, sourceFeed))
				.collect(Collectors.toList());
		
		sourceFeedEntryRepository.saveAll(sourceFeedEntries);
	}


	public void deleteSourceFeedEntries(SourceFeed sourceFeed) {
		deleteSourceFeedEntries(sourceFeed.getId());
	}
	
	
	public void deleteSourceFeedEntries(long sourceFeedId) {
		sourceFeedEntryRepository.deleteBySourceFeedId(sourceFeedId);
	}
	
	
	private SyndEntry readSourceFeedEntryContent(SourceFeedEntry sourceFeedEntry) {
		try (var input = new ByteArrayInputStream(sourceFeedEntry.getContent())) {
			var entryFeed = syndFeedInput.build(new XmlReader(input));
			return entryFeed.getEntries().get(0);
		}
		catch (IOException | IllegalArgumentException | FeedException e) {
			logger.error(String.format("Failed to read content of source feed entry %d", sourceFeedEntry.getId()), e);
			return null;
		}
	}
	
	
	private SourceFeedEntry createSourceFeedEntry(SyndEntry syndEntry, String feedType, SourceFeed sourceFeed) {
		var entryFeed = createEntryFeed(syndEntry, feedType);
		
		var date = getEntryDate(syndEntry);
		if (date != null) {
			return new SourceFeedEntry(date, getContentBytes(entryFeed), sourceFeed);
		}
		else {
			var message = String.format("Could not create source feed entry for SyndEntry instance "
					+ "of source feed %s because it has neither published nor updated date",
					sourceFeed.getSource());
			logger.warn(message);
			return null;
		}
	}
	
	
	private static SyndFeed createEntryFeed(SyndEntry syndEntry, String feedType) {
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
