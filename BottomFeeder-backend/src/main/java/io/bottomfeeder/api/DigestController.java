package io.bottomfeeder.api;

import static io.bottomfeeder.config.Constants.API_URL_DIGESTS;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bottomfeeder.api.model.DigestRequest;
import io.bottomfeeder.api.model.DigestResponse;
import io.bottomfeeder.api.model.DigestTitleResponse;
import io.bottomfeeder.api.model.Response;
import io.bottomfeeder.data.DataExportService;
import io.bottomfeeder.digest.Digest;
import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.digest.feed.DigestFeedFormat;
import io.bottomfeeder.security.Role;
import io.bottomfeeder.security.permission.PermissionExpressions;
import io.bottomfeeder.user.UserService;

/**
 * REST controller for managing digests.
 */
@RestController
@RequestMapping(API_URL_DIGESTS)
class DigestController {

	private final DigestService digestService;
	private final UserService userService;
	private final DataExportService dataExportService;

	
	public DigestController(
			DigestService digestService, 
			UserService userService, 
			DataExportService dataExportService) {
		this.digestService = digestService;
		this.userService = userService;
		this.dataExportService = dataExportService;
	}
	
	
	@Secured(Role.Name.ADMIN)
	@GetMapping("/all")
	public Response<List<DigestResponse>> getAllDigests() {
		return createDigestsResponse(digestService.getAllDigests());
	}

	
	@Secured(Role.Name.ADMIN)
	@GetMapping("/all/titles")
	public Response<List<DigestTitleResponse>> getAllDigestTitles() {
		return createDigestTitlesResponse(digestService.getAllDigests());
	}

	
	@GetMapping("/own")
	public Response<List<DigestResponse>> getOwnDigests() {
		return createDigestsResponse(digestService.getOwnerDigests(userService.getAuthenticatedUser()));
	}

	
	@GetMapping("/own/titles")
	public Response<List<DigestTitleResponse>> getOwnDigestTitles() {
		return createDigestTitlesResponse(digestService.getOwnerDigests(userService.getAuthenticatedUser()));
	}
	
	
	private static Response<List<DigestTitleResponse>> createDigestTitlesResponse(List<Digest> digests) {
		var digestTitles = digests.stream()
				.map(DigestTitleResponse::new)
				.sorted(Comparator.comparing(DigestTitleResponse::title, String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
		return new Response<>(digestTitles);
	}
	
	
	@PreAuthorize(PermissionExpressions.READ_DIGEST)
	@GetMapping("/{id}")
	public Response<DigestResponse> getDigest(@PathVariable long id) {
		return new Response<>(createDigestResponse(digestService.getDigest(id)));
	}
	
	
	@PostMapping
	public Response<DigestResponse> createDigest(@Valid @RequestBody DigestRequest digestRequest) {
		var owner = userService.getAuthenticatedUser();
		var digest = digestService.createDigest(owner, digestRequest.title(), 
				digestRequest.maxItems(), digestRequest.isPrivate());
		var message = String.format("Digest '%s' created successfully", digest.getTitle());
		return new Response<>(message, createDigestResponse(digest));
	}
	
	
	@PreAuthorize(PermissionExpressions.UPDATE_DIGEST)
	@PutMapping
	public Response<DigestResponse> updateDigest(@Valid @RequestBody DigestRequest digestRequest) {
		var updatedDigest = digestService.updateDigest(digestRequest.id(), digestRequest.title(), 
				digestRequest.maxItems(), digestRequest.isPrivate());
		var message = String.format("Digest '%s' updated successfully", updatedDigest.getTitle());
		return new Response<>(message, createDigestResponse(updatedDigest));
	}
	
	
	@PreAuthorize(PermissionExpressions.DELETE_DIGEST)
	@DeleteMapping("/{id}")
	public Response<Void> deleteDigest(@PathVariable long id) {
		digestService.deleteDigest(id);
		return new Response<>("Digest deleted successfully");
	}
	
	
	@GetMapping("/own/export")
	public ResponseEntity<byte[]> exportOwnDigests() {
		return Utils.createBinaryJsonResponse(dataExportService.exportDigestsData(userService.getAuthenticatedUser()));
	}
	
	
	private Response<List<DigestResponse>> createDigestsResponse(List<Digest> digests) {
		return new Response<>(digests.stream().map(this::createDigestResponse).collect(Collectors.toList()));
	}
	
	
	private DigestResponse createDigestResponse(Digest digest) {
		var externalId = digest.getExternalId();
		var rssLink = digestService.getDigestFeedLink(externalId, DigestFeedFormat.RSS_2_0);
		var atomLink = digestService.getDigestFeedLink(externalId, DigestFeedFormat.ATOM_1_0);
		return new DigestResponse(digest, rssLink, atomLink);
	}
	
}
