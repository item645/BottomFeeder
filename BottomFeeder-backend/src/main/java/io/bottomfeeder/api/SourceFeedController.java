package io.bottomfeeder.api;

import static io.bottomfeeder.config.Constants.API_URL_SOURCE_FEEDS;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.api.model.SourceFeedRequest;
import io.bottomfeeder.api.model.SourceFeedResponse;
import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.security.permission.PermissionExpressions;
import io.bottomfeeder.sourcefeed.SourceFeedService;

/**
 * REST controller for managing source feeds.
 */
@RestController
@RequestMapping(API_URL_SOURCE_FEEDS)
class SourceFeedController {

	private final SourceFeedService sourceFeedService;
	private final DigestService digestService;
	
	public SourceFeedController(SourceFeedService sourceFeedService, DigestService digestService) {
		this.sourceFeedService = sourceFeedService;
		this.digestService = digestService;
	}
	
	
	@PreAuthorize(PermissionExpressions.READ_DIGEST)
	@GetMapping("/digest/{id}")
	public Response<List<SourceFeedResponse>> getDigestSourceFeeds(@PathVariable long id) {
		var digest = digestService.getDigest(id);
		return new Response<>(sourceFeedService.getDigestSourceFeeds(digest).stream()
				.map(SourceFeedResponse::new)
				.collect(Collectors.toList()));
	}
	
	
	@PreAuthorize(PermissionExpressions.READ_SOURCE_FEED)
	@GetMapping("/{id}")
	public Response<SourceFeedResponse> getSourceFeed(@PathVariable long id) {
		return new Response<>(new SourceFeedResponse(sourceFeedService.getSourceFeed(id)));
	}
	
	
	@PreAuthorize(PermissionExpressions.CREATE_SOURCE_FEED_FOR_DIGEST)
	@PostMapping
	public Response<SourceFeedResponse> createSourceFeed(@Valid @RequestBody SourceFeedRequest sourceFeedRequest) {
		var digest = digestService.getDigest(sourceFeedRequest.digestId());
		var sourceFeed = sourceFeedService.createSourceFeed(digest, sourceFeedRequest.source(), 
				sourceFeedRequest.contentUpdateInterval(), sourceFeedRequest.updateContent());
		var message = String.format("Source feed with source '%s' for digest '%s' created successfully", 
				sourceFeed.getTruncatedSource(), digest.getTitle());
		return new Response<>(message, new SourceFeedResponse(sourceFeed));
	}
	
	
	@PreAuthorize(PermissionExpressions.UPDATE_SOURCE_FEED)
	@PutMapping
	public Response<SourceFeedResponse> updateSourceFeed(@Valid @RequestBody SourceFeedRequest sourceFeedRequest) {
		var updatedSourceFeed = sourceFeedService.updateSourceFeed(sourceFeedRequest.id(), 
				sourceFeedRequest.digestId(), sourceFeedRequest.source(), 
				sourceFeedRequest.contentUpdateInterval(), sourceFeedRequest.updateContent());
		var message = String.format("Source feed '%s' updated successfully", updatedSourceFeed.getTruncatedSource());
		return new Response<>(message, new SourceFeedResponse(updatedSourceFeed));
	}
	
	
	@PreAuthorize(PermissionExpressions.DELETE_SOURCE_FEED)
	@DeleteMapping("/{id}")
	public Response<Void> deleteSourceFeed(@PathVariable long id) {
		sourceFeedService.deleteSourceFeed(id);
		return new Response<>("Source feed deleted successfully");
	}

}
