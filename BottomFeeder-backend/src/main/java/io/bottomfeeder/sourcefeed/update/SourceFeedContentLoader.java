package io.bottomfeeder.sourcefeed.update;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

import io.bottomfeeder.sourcefeed.SourceFeed;
import io.bottomfeeder.sourcefeed.SourceFeedContent;
import io.bottomfeeder.sourcefeed.SourceFeedException;

/**
 * A callable task that implements functionality for obtaining latest content from
 * the source for specified source feed.
 * 
 * The task does not actually update the source feed entity, instead it returns updated 
 * content as {@code SourceFeedContent} instance upon successful completion, and additionaly
 * allows to set callback functions to be invoked on various stages of its execution.
 */
class SourceFeedContentLoader implements Callable<SourceFeedContent> {
	// TODO use time-based cache to hold fetched feed content for short time

	private final SourceFeed sourceFeed;
	private final Consumer<SourceFeed> onStart;
	private final BiConsumer<SourceFeed, SourceFeedContent> onSuccess;
	private final BiConsumer<SourceFeed, Throwable> onFailure;
	private final Consumer<SourceFeed> onComplete;
	
	
	/**
	 * A builder for content loader task.
	 */
	static final class Builder {
		
		private final SourceFeed sourceFeed;
		private Consumer<SourceFeed> onStart = sourceFeed -> {};
		private BiConsumer<SourceFeed, SourceFeedContent> onSuccess = (sourceFeed, updatedContent) -> {};
		private BiConsumer<SourceFeed, Throwable> onFailure = (sourceFeed, exception) -> {};
		private Consumer<SourceFeed> onComplete = sourceFeed -> {};
		
		Builder(SourceFeed sourceFeed) {
			this.sourceFeed = requireNonNull(sourceFeed);
		}
		
		Builder onStart(Consumer<SourceFeed> onStart) {
			this.onStart = requireNonNull(onStart);
			return this;
		}
		
		Builder onSuccess(BiConsumer<SourceFeed, SourceFeedContent> onSuccess) {
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
		
		FutureTask<SourceFeedContent> buildFutureTask() {
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
	public SourceFeedContent call() {
		onStart.accept(sourceFeed);
		
		var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		var request = HttpRequest.newBuilder(sourceFeed.getURI()).build();
		
		// TODO handle HTTP 429/503 using exponential backoff or similar strategy
		try (var input = httpClient.send(request, BodyHandlers.ofInputStream()).body()) {
			var updatedContent = getUpdatedContent(new SyndFeedInput().build(new XmlReader(input)));
			onSuccess.accept(sourceFeed, updatedContent);
			return updatedContent;
		}
		catch (Exception exception) {
			if (exception instanceof InterruptedException)
				Thread.currentThread().interrupt();	
			onFailure.accept(sourceFeed, exception);
			
			var msg = String.format("Error updating content of source feed %s", sourceFeed.getTruncatedSource());
			throw new SourceFeedException(msg, exception);
		}
		finally {
			onComplete.accept(sourceFeed);
		}
	}

	
	private SourceFeedContent getUpdatedContent(SyndFeed newFeedData) throws IOException, FeedException {
		var writer = new StringWriter();
		new SyndFeedOutput().output(newFeedData, writer);
		
		var content = writer.toString();
		var updateDate = Instant.now();
		var title = StringUtils.abbreviate(StringUtils.trimToNull(newFeedData.getTitle()), SourceFeed.TITLE_MAX_SIZE);
		
		return new SourceFeedContent(title, content, updateDate);
	}
	
}
