package io.bottomfeeder.sourcefeed.update;

import static java.util.Objects.requireNonNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.sourcefeed.SourceFeedException;

/**
 * A callable task that implements functionality for obtaining latest content from
 * the source for specified source feed.
 * 
 * The task does not actually update the source feed entity, instead it returns latest 
 * content as {@code SyndFeed} instance upon successful completion, and additionaly
 * allows to set callback functions to be invoked on various stages of its execution.
 */
class SourceFeedContentLoader implements Callable<SyndFeed> {
	// TODO use time-based cache to hold fetched feed content for short time

	private final SourceFeed sourceFeed;
	private final Consumer<SourceFeed> onStart;
	private final BiConsumer<SourceFeed, SyndFeed> onSuccess;
	private final BiConsumer<SourceFeed, Throwable> onFailure;
	private final Consumer<SourceFeed> onComplete;
	
	
	/**
	 * A builder for content loader task.
	 */
	static final class Builder {
		
		private final SourceFeed sourceFeed;
		private Consumer<SourceFeed> onStart = sourceFeed -> {};
		private BiConsumer<SourceFeed, SyndFeed> onSuccess = (sourceFeed, updatedContent) -> {};
		private BiConsumer<SourceFeed, Throwable> onFailure = (sourceFeed, exception) -> {};
		private Consumer<SourceFeed> onComplete = sourceFeed -> {};
		
		Builder(SourceFeed sourceFeed) {
			this.sourceFeed = requireNonNull(sourceFeed);
		}
		
		Builder onStart(Consumer<SourceFeed> onStart) {
			this.onStart = requireNonNull(onStart);
			return this;
		}
		
		Builder onSuccess(BiConsumer<SourceFeed, SyndFeed> onSuccess) {
			this.onSuccess = requireNonNull(onSuccess);
			return this;
		}
		
		Builder onFailure(BiConsumer<SourceFeed, Throwable> onFailure) {
			this.onFailure = requireNonNull(onFailure);
			return this;
		}
		
		Builder onComplete(Consumer<SourceFeed> onComplete) {
			this.onComplete = requireNonNull(onComplete);
			return this;
		}
		
		SourceFeedContentLoader build() {
			return new SourceFeedContentLoader(this);
		}
		
		FutureTask<SyndFeed> buildFutureTask() {
			return new FutureTask<>(build());
		}
	}
	
	
	private SourceFeedContentLoader(Builder builder) {
		this.sourceFeed = builder.sourceFeed;
		this.onStart = builder.onStart;
		this.onSuccess = builder.onSuccess;
		this.onFailure = builder.onFailure;
		this.onComplete = builder.onComplete;
	}


	@Override
	public SyndFeed call() {
		onStart.accept(sourceFeed);
		
		var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		var request = HttpRequest.newBuilder(sourceFeed.getURI()).build();
		
		// TODO handle HTTP 429/503 using exponential backoff or similar strategy
		try (var input = httpClient.send(request, BodyHandlers.ofInputStream()).body()) {
			var newFeedData = new SyndFeedInput().build(new XmlReader(input));
			onSuccess.accept(sourceFeed, newFeedData);
			return newFeedData;
		}
		catch (Exception exception) {
			if (exception instanceof InterruptedException)
				Thread.currentThread().interrupt();	
			onFailure.accept(sourceFeed, exception);
			
			var msg = String.format("Error loading content for source feed %s", sourceFeed.getTruncatedSource());
			throw new SourceFeedException(msg, exception);
		}
		finally {
			onComplete.accept(sourceFeed);
		}
	}
	
}
