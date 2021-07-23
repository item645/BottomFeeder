package io.bottomfeeder.sourcefeed.update;

import static java.lang.String.format;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.sourcefeed.SourceFeedContent;
import io.bottomfeeder.sourcefeed.SourceFeedException;
import io.bottomfeeder.sourcefeed.SourceFeedRepository;
import io.bottomfeeder.util.TransactionalRunner;

/**
 * A service responsible for updating source feeds with latest version of the content.
 * It provides functionality for scheduled update of source feeds as well as loading
 * latest content on demand and cancelling currently running update for the specified feed.
 * 
 * Scheduled update for each feed is triggered by scheduled task running at fixed time
 * interval, taking into account source feed's configured content update interval.
 */
@Service
public class SourceFeedContentUpdateService {
	
	private static final Logger logger = LoggerFactory.getLogger(SourceFeedContentUpdateService.class);
	
	private final SourceFeedRepository sourceFeedRepository;
	private final ThreadPoolTaskExecutor taskExecutor;
	private final TransactionalRunner transactionalRunner;
	
	private final ConcurrentHashMap<Long, FutureTask<SourceFeedContent>> updaters = new ConcurrentHashMap<>();

	
	public SourceFeedContentUpdateService(
			SourceFeedRepository sourceFeedRepository, 
			ThreadPoolTaskExecutor taskExecutor, 
			TransactionalRunner transactionalRunner) {
		this.sourceFeedRepository = sourceFeedRepository;
		this.taskExecutor = taskExecutor;
		this.transactionalRunner = transactionalRunner;
	}


	@Scheduled(fixedDelayString = "#{${bf.scheduler.source-feed-update-interval-minutes} * 60000}")
	public void runScheduledUpdate() {
		logger.info("Starting scheduled update of source feeds...");
		
		sourceFeedRepository.findAllSummaries().stream()
			.filter(this::isReadyForUpdate)
			.forEach(this::runScheduledFeedUpdate);
	}

	
	public SourceFeedContent loadLatestContent(SourceFeed sourceFeed) {
		return sourceFeed.getId() == null 
				? loadLatestContentForNewFeed(sourceFeed) : loadLatestContentForExistingFeed(sourceFeed);
	}

	
	public void cancelUpdate(long sourceFeedId) {
		var updater = updaters.remove(sourceFeedId);
		if (updater != null) {
			logger.info(format("Cancelling update for source feed ID: %d", sourceFeedId));
			updater.cancel(true);
		}
	}
	
	
	private void runScheduledFeedUpdate(SourceFeed sourceFeed) {
		var updater = updaters.computeIfAbsent(sourceFeed.getId(), id -> createScheduledUpdateTask(sourceFeed));
		taskExecutor.execute(updater);
	}
	
	
	private SourceFeedContent loadLatestContentForNewFeed(SourceFeed sourceFeed) {
		return new SourceFeedContentLoader.Builder(sourceFeed)
				.onStart(this::reportOnDemandContentLoadStart)
				.build()
				.call();
	}
	
	
	private SourceFeedContent loadLatestContentForExistingFeed(SourceFeed sourceFeed) {
		try {
			// When there is an executing updater task for this feed, we don't start a new one,
			// instead we are joining in and wait for shared result to be available.
			// It is caller's responsibility to ensure that this result is still relevant
			// (i.e. that feed's source property has not changed).
			var updater = updaters.computeIfAbsent(sourceFeed.getId(), id -> {
				return new SourceFeedContentLoader.Builder(sourceFeed)
						.onStart(this::reportOnDemandContentLoadStart)
						.onComplete(this::removeUpdater)
						.buildFutureTask();
			});
			updater.run(); // no-op if already running
			return updater.get();
		}
		catch (Throwable exception) {
			var cause = exception;
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			else if (exception instanceof ExecutionException) {
				cause = Objects.requireNonNullElse(exception.getCause(), exception);
			}
			if (cause instanceof SourceFeedException sfe)
				throw sfe;
			else
				throw new SourceFeedException(cause);		
		}
	}
	
	
	private FutureTask<SourceFeedContent> createScheduledUpdateTask(SourceFeed sourceFeed) {
		return new SourceFeedContentLoader.Builder(sourceFeed)
				.onStart(this::reportScheduledUpdateStart)
				.onSuccess(this::saveUpdatedContent)
				.onFailure(this::reportUpdateError)
				.onComplete(this::removeUpdater)
				.buildFutureTask();	
	}

	
	private void reportScheduledUpdateStart(SourceFeed sourceFeed) {
		ensureHasId(sourceFeed);
		logger.info(format("Running scheduled update for %s", getFeedInfo(sourceFeed)));
	}
	
	
	private void reportOnDemandContentLoadStart(SourceFeed sourceFeed) {
		logger.info(format("Loading latest content on demand for %s", getFeedInfo(sourceFeed)));
	}
	
	
	private void reportUpdateError(SourceFeed sourceFeed, Throwable exception) {
		logger.warn(format("Update failed for %s. Error message: %s", getFeedInfo(sourceFeed), exception));
	}
	
	
	private static String getFeedInfo(SourceFeed sourceFeed) {
		var id = sourceFeed.getId();
		var source = sourceFeed.getSource();
		return id != null ? format("source feed %s (ID: %d)", source, id) : format("source feed %s", source);
	}
	
	
	private void saveUpdatedContent(SourceFeed sourceFeed, SourceFeedContent updatedContent) {
		ensureHasId(sourceFeed);
		if (updaters.containsKey(sourceFeed.getId())) {
			transactionalRunner.run(() -> {
				sourceFeedRepository.findSummaryAndLockById(sourceFeed.getId()).ifPresent(currentSourceFeed -> {
					currentSourceFeed.setUpdatedContent(updatedContent);
					sourceFeedRepository.save(currentSourceFeed);
					logger.info(format("Updated %s with latest data", getFeedInfo(sourceFeed)));
				});
			});
		}
	}
	
	
	private void removeUpdater(SourceFeed sourceFeed) {
		ensureHasId(sourceFeed);
		logger.info(format("Removing updater for %s from list of running updaters", getFeedInfo(sourceFeed)));
		updaters.remove(sourceFeed.getId());
	}


	private boolean isReadyForUpdate(SourceFeed sourceFeed) {
		var lastUpdateDate = sourceFeed.getContentUpdateDate();
		if (lastUpdateDate == null)
			return true;
		else
			return ChronoUnit.MINUTES.between(lastUpdateDate, Instant.now()) >= sourceFeed.getContentUpdateInterval();
	}

	
	private static void ensureHasId(SourceFeed sourceFeed) {
		assert sourceFeed.getId() != null;
	}

}
