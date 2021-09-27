package io.bottomfeeder.api;

import static io.bottomfeeder.config.Constants.API_URL_ENTRY_FILTERS;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.filter.DigestEntryFilter;
import io.bottomfeeder.filter.EntryFilterService;
import io.bottomfeeder.filter.SourceFeedEntryFilter;
import io.bottomfeeder.filter.model.DigestEntryFilterData;
import io.bottomfeeder.filter.model.DigestEntryFilterList;
import io.bottomfeeder.filter.model.SourceFeedEntryFilterData;
import io.bottomfeeder.filter.model.SourceFeedEntryFilterList;
import io.bottomfeeder.security.permission.PermissionExpressions;
import io.bottomfeeder.sourcefeed.SourceFeedService;

/**
 * REST controller for managing entry filters.
 */
@RestController
@RequestMapping(API_URL_ENTRY_FILTERS)
class EntryFilterController {

	private final EntryFilterService entryFilterService;
	private final DigestService digestService;
	private final SourceFeedService sourceFeedService;
	
	
	public EntryFilterController(
			EntryFilterService entryFilterService, 
			DigestService digestService, 
			SourceFeedService sourceFeedService) {
		this.entryFilterService = entryFilterService;
		this.digestService = digestService;
		this.sourceFeedService = sourceFeedService;
	}

	
	@InitBinder
	void configureDirectFieldAccess(DataBinder dataBinder) {
		// Force field-based access to properties of filter models because we don't use 
		// conventionally named getters there  
		dataBinder.initDirectFieldAccess();
	}

	
	@PreAuthorize(PermissionExpressions.READ_DIGEST)
	@GetMapping("/digest/{id}")
	public Response<DigestEntryFilterList> getDigestEntryFilters(@PathVariable long id) {
		var filters = entryFilterService.getDigestEntryFilters(digestService.getDigest(id));
		return createDigestEntryFilterListResponse(filters, null);
	}
	
	
	@PreAuthorize(PermissionExpressions.UPDATE_DIGEST_ENTRY_FILTERS)
	@PostMapping("/digest/{id}")
	public Response<DigestEntryFilterList> updateDigestEntryFilters(@PathVariable long id,
			@Valid @RequestBody DigestEntryFilterList filterListRequest) {
		var digest = digestService.getDigest(id);

		var updatedFilters = entryFilterService.updateDigestEntryFilters(filterListRequest, digest);
		var message = String.format("Filter list for digest '%s' has been updated", digest.getTitle());
		
		return createDigestEntryFilterListResponse(updatedFilters, message);
	}

	
	@PreAuthorize(PermissionExpressions.READ_SOURCE_FEED)
	@GetMapping("/feed/{id}")
	public Response<SourceFeedEntryFilterList> getSourceFeedEntryFilters(@PathVariable long id) {
		var filters = entryFilterService.getSourceFeedEntryFilters(sourceFeedService.getSourceFeed(id));
		return createSourceFeedEntryFilterListResponse(filters, null);
	}
	
	
	@PreAuthorize(PermissionExpressions.UPDATE_SOURCE_FEED_ENTRY_FILTERS)
	@PostMapping("/feed/{id}")
	public Response<SourceFeedEntryFilterList> updateSourceFeedEntryFilters(@PathVariable long id,
			@Valid @RequestBody SourceFeedEntryFilterList filterListRequest) {
		var sourceFeed = sourceFeedService.getSourceFeed(id);

		var updatedFilters = entryFilterService.updateSourceFeedEntryFilters(filterListRequest, sourceFeed);
		var message = String.format("Filter list for source feed '%s' has been updated", sourceFeed.getTruncatedSource());	
		
		return createSourceFeedEntryFilterListResponse(updatedFilters, message);
	}
	
	
	private static Response<DigestEntryFilterList> createDigestEntryFilterListResponse(
			List<DigestEntryFilter> filters, String message) {
		var filterListResponse = filters.stream().map(DigestEntryFilterData::new).collect(toList());
		return new Response<>(message, new DigestEntryFilterList(filterListResponse));
	}
	
	
	private static Response<SourceFeedEntryFilterList> createSourceFeedEntryFilterListResponse(
			List<SourceFeedEntryFilter> filters, String message) {
		var filterListResponse = filters.stream().map(SourceFeedEntryFilterData::new).collect(toList());
		return new Response<>(message, new SourceFeedEntryFilterList(filterListResponse));
	}
	
}
