package io.bottomfeeder.digest.feed;

import static io.bottomfeeder.config.Constants.DIGEST_FEED_URL;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.View;

import io.bottomfeeder.digest.DigestService;
import io.bottomfeeder.security.permission.PermissionExpressions;

/**
 * A controller providing access to aggregated RSS/Atom output feed of the digest.
 */
@Controller
@RequestMapping(DIGEST_FEED_URL)
class DigestFeedController {

	private static final Logger logger = LoggerFactory.getLogger(DigestFeedController.class);
	
	private final DigestService digestService;
	
	public DigestFeedController(DigestService digestService) {
		this.digestService = digestService;
	}


	@PreAuthorize(PermissionExpressions.READ_DIGEST_FEED)
	@GetMapping("/{digestExternalId}/feed.rss")
	public View getRssDigestFeed(@PathVariable String digestExternalId) {
		return getDigestFeedView(digestExternalId, DigestFeedFormat.RSS_2_0);
	}
	
	
	@PreAuthorize(PermissionExpressions.READ_DIGEST_FEED)
	@GetMapping("/{digestExternalId}/feed.atom")
	public View getAtomDigestFeed(@PathVariable String digestExternalId) {
		return getDigestFeedView(digestExternalId, DigestFeedFormat.ATOM_1_0);
	}
	
	
	private View getDigestFeedView(String digestExternalId, DigestFeedFormat digestFeedFormat) {
		return new DigestFeedView(digestFeedFormat, digestService.getDigestFeed(digestExternalId, digestFeedFormat));
	}
	
	
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public void handleAccessDeniedError(Exception exception, HttpServletRequest request) {
		logger.warn(String.format("Access to digest feed is denied: %s", request.getRequestURI()));
	}
	
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleError(Exception exception, HttpServletRequest request) {
		logger.error(String.format("Error accessing digest feed: %s", request.getRequestURI()), exception);
	}
	
}
