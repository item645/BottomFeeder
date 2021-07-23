package io.bottomfeeder.sourcefeed;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.DigestRepository;
import io.bottomfeeder.sourcefeed.update.SourceFeedContentUpdateService;

/**
 * A service providing common functionality for working with source feeds.
 */
@Service
public class SourceFeedService {

	private static final Logger logger = LoggerFactory.getLogger(SourceFeedService.class);
	
	private final SourceFeedRepository sourceFeedRepository;
	private final SourceFeedContentUpdateService sourceFeedContentUpdateService;
	private final DigestRepository digestRepository;

	
	public SourceFeedService(
			SourceFeedRepository sourceFeedRepository, 
			SourceFeedContentUpdateService sourceFeedContentUpdateService,
			DigestRepository digestRepository) {
		this.sourceFeedRepository = sourceFeedRepository;
		this.sourceFeedContentUpdateService = sourceFeedContentUpdateService;
		this.digestRepository = digestRepository;
	}
	
	
	public SourceFeed getSourceFeed(long id) {
		return getSourceFeed(sourceFeedRepository::findSummaryById, id);
	}

	
	public List<SourceFeed> getDigestSourceFeeds(Digest digest) {
		return sourceFeedRepository.findSummariesByDigest(digest);
	}
	
	
	@Transactional
	public SourceFeed createSourceFeed(Digest digest, String source, 
			int contentUpdateInterval, boolean updateContent) {
		source = normalizeSource(source);
		if (sourceFeedRepository.existsBySourceAndDigest(source, digest))
			throw duplicateSourceFeedError(source, digest);
		
		var sourceFeed = new SourceFeed(source, contentUpdateInterval, digest);
		if (updateContent)
			sourceFeed.setUpdatedContent(sourceFeedContentUpdateService.loadLatestContent(sourceFeed));
		
		return sourceFeedRepository.save(sourceFeed);
	}
	
	
	@Transactional
	public SourceFeed updateSourceFeed(long id, long newDigestId, String newSource, 
			int newContentUpdateInterval, boolean updateContent) {
		var sourceFeed = getSourceFeed(sourceFeedRepository::findAndLockById, id);
		
		var currentDigest = sourceFeed.getDigest();
		var digestChanged = !currentDigest.getId().equals(newDigestId);
		var newDigest = digestChanged ? getDigest(newDigestId) : currentDigest;
		
		newSource = normalizeSource(newSource);
		var sourceChanged = !sourceFeed.getSource().equals(newSource);
		
		if (sourceChanged || digestChanged) {
			if (sourceFeedRepository.existsBySourceAndDigest(newSource, newDigest))
				throw duplicateSourceFeedError(newSource, newDigest);
		}
		
		if (sourceChanged) {
			sourceFeed.setSource(newSource);
			sourceFeedContentUpdateService.cancelUpdate(id);
			if (!updateContent)
				sourceFeed.setUpdatedContent(null);
		}
		
		sourceFeed.setDigest(newDigest);
		sourceFeed.setContentUpdateInterval(newContentUpdateInterval);
			
		if (updateContent)
			sourceFeed.setUpdatedContent(sourceFeedContentUpdateService.loadLatestContent(sourceFeed));
		
		return sourceFeedRepository.save(sourceFeed);
	}
	
	
	private static SourceFeed getSourceFeed(Function<Long, Optional<SourceFeed>> finder, long id) {
		return finder.apply(id).orElseThrow(
				() -> new SourceFeedException(format("Source feed with id '%d' not found", id)));
	}
	
	
	private Digest getDigest(long id) {
		return digestRepository.findById(id).orElseThrow(
				() -> new SourceFeedException(format("Cannot set new digest for source feed"
						+ " because digest with id '%d' not found", id)));
	}
	
	
	private static String normalizeSource(String source) {
		source = StringUtils.trimToEmpty(source);
		
		var sourceUri = URI.create(source);
		var scheme = StringUtils.toRootLowerCase(sourceUri.getScheme());
		if (scheme == null)
			throw new SourceFeedException(format("Invalid source '%s': scheme part not specified", source));
		
		var authority = StringUtils.toRootLowerCase(sourceUri.getAuthority());
		if (authority == null)
			throw new SourceFeedException(format("Invalid source '%s': authority part not specified", source));
		
		var path = sourceUri.getPath();
		var query = sourceUri.getQuery();
		var fragment = sourceUri.getFragment();
		
		var builder = new StringBuilder(source.length()).append(scheme).append("://").append(authority);
		if (path != null) 
			builder.append(path);
		if (query != null) 
			builder.append('?').append(query);
		if (fragment != null) 
			builder.append("#").append(fragment);
		
		return builder.toString();
	}
	
	
	private static SourceFeedException duplicateSourceFeedError(String source, Digest digest) {
		return new SourceFeedException(format("Source feed for source '%s' already exists for digest '%s'",
				source, digest.getTitle()));
	}
	
	
	public void deleteSourceFeed(long id) {
		sourceFeedContentUpdateService.cancelUpdate(id);
		sourceFeedRepository.deleteById(id);
	}
	
	
	public void deleteDigestSourceFeeds(Digest digest) {
		sourceFeedRepository.findIdsByDigest(digest).stream().forEach(sourceFeedContentUpdateService::cancelUpdate);
		sourceFeedRepository.deleteByDigest(digest);
	}
	
	
	@Transactional
	public Collection<SyndFeed> loadDigestSourceFeedsContent(Digest digest) {
		return sourceFeedRepository.findByDigest(digest).stream()
				.map(SourceFeedService::readSourceFeedContent)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	
	private static SyndFeed readSourceFeedContent(SourceFeed sourceFeed) {
		var content = sourceFeed.getContent();
		if (!StringUtils.isBlank(content)) {
			try (var input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
				return new SyndFeedInput().build(new XmlReader(input));
			}
			catch (IllegalArgumentException | FeedException | IOException e) {
				logger.error(format("Failed to read content of source feed %s while building a digest feed: %s",
						sourceFeed.getSource(), sourceFeed.getDigest().getExternalId(), e));
				return null;
			}
		}
		else {
			return null;
		}
	}
	
}
