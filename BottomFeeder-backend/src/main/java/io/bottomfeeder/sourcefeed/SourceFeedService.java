package io.bottomfeeder.sourcefeed;

import static java.lang.String.format;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.DigestRepository;
import io.bottomfeeder.sourcefeed.entry.SourceFeedEntryService;
import io.bottomfeeder.sourcefeed.update.SourceFeedContentUpdateService;

/**
 * A service providing common functionality for working with source feeds.
 */
@Service
public class SourceFeedService {

	private final SourceFeedRepository sourceFeedRepository;
	private final SourceFeedContentUpdateService sourceFeedContentUpdateService;
	private final SourceFeedEntryService sourceFeedEntryService;
	private final DigestRepository digestRepository;

	
	public SourceFeedService(
			SourceFeedRepository sourceFeedRepository, 
			SourceFeedContentUpdateService sourceFeedContentUpdateService,
			SourceFeedEntryService sourceFeedEntryService,
			DigestRepository digestRepository) {
		this.sourceFeedRepository = sourceFeedRepository;
		this.sourceFeedContentUpdateService = sourceFeedContentUpdateService;
		this.sourceFeedEntryService = sourceFeedEntryService;
		this.digestRepository = digestRepository;
	}
	
	
	public SourceFeed getSourceFeed(long id) {
		return getSourceFeed(sourceFeedRepository::findById, id);
	}

	
	public List<SourceFeed> getDigestSourceFeeds(Digest digest) {
		return sourceFeedRepository.findByDigestOrderByCreationDateDesc(digest);
	}
	
	
	@Transactional
	public SourceFeed createSourceFeed(Digest digest, String source, 
			int contentUpdateInterval, boolean updateContent) {
		source = normalizeSource(source);
		if (sourceFeedRepository.existsBySourceAndDigest(source, digest))
			throw duplicateSourceFeedError(source, digest);
		
		var sourceFeed = new SourceFeed(source, contentUpdateInterval, digest);
		return updateContent ? updateContentAndSave(sourceFeed) : sourceFeedRepository.save(sourceFeed);
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
				// If source changed, purge content-related data as it's no longer relevant 
				// and must be updated anyway
				purgeContent(sourceFeed);
		}
		
		sourceFeed.setDigest(newDigest);
		sourceFeed.setContentUpdateInterval(newContentUpdateInterval);
		
		return updateContent ? updateContentAndSave(sourceFeed) : sourceFeedRepository.save(sourceFeed);
	}
	
	
	private SourceFeed updateContentAndSave(SourceFeed sourceFeed) {
		var newFeedData = sourceFeedContentUpdateService.loadLatestContent(sourceFeed);
		sourceFeed.setAbbreviatedTitle(newFeedData.getTitle());
		sourceFeed.setContentUpdateDate(Instant.now());
		
		sourceFeed = sourceFeedRepository.save(sourceFeed);
		sourceFeedEntryService.replaceSourceFeedEntries(newFeedData, sourceFeed);
		
		return sourceFeed;
	}
	
	
	private void purgeContent(SourceFeed sourceFeed) {
		assert sourceFeed.getId() != null;
		
		sourceFeedEntryService.deleteSourceFeedEntries(sourceFeed);
		sourceFeed.setTitle(null);
		sourceFeed.setContentUpdateDate(null);
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
	
	
	@Transactional
	public void deleteSourceFeed(long id) {
		sourceFeedContentUpdateService.cancelUpdate(id);
		sourceFeedEntryService.deleteSourceFeedEntries(id);
		sourceFeedRepository.deleteById(id);
	}
	
	
	@Transactional
	public void deleteDigestSourceFeeds(Digest digest) {
		sourceFeedRepository.findIdsByDigest(digest).stream().forEach(this::deleteSourceFeed);
	}
	
}
