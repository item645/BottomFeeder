package io.bottomfeeder.security.permission;

/**
 * Helper class providing compile-time constants for permission-checking SpEL-expressions
 * which are used in {@code @PreAuthorize/@PostAuthorize} annotations on methods in controllers.
 */
public final class PermissionExpressions {

	private PermissionExpressions() {}
	
	private static final String DIGEST_CLASS = "io.bottomfeeder.digest.Digest";
	private static final String SOURCE_FEED_CLASS = "io.bottomfeeder.sourcefeed.SourceFeed";
	private static final String PERMISSION_CLASS = "io.bottomfeeder.security.permission.Permission";
	
	public static final String READ_DIGEST = 
			"hasPermission(#id, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").READ)";
	
	public static final String UPDATE_DIGEST = 
			"hasPermission(#digestRequest, T(" + PERMISSION_CLASS + ").UPDATE)";
	
	public static final String DELETE_DIGEST = 
			"hasPermission(#id, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").DELETE)";
	
	public static final String READ_DIGEST_FEED = 
			"hasPermission(#digestExternalId, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").READ_DIGEST_FEED)";
	
	public static final String READ_SOURCE_FEED = 
			"hasPermission(#id, '" + SOURCE_FEED_CLASS + "', T(" + PERMISSION_CLASS + ").READ)";
	
	public static final String CREATE_SOURCE_FEED_FOR_DIGEST = 
			"hasPermission(#sourceFeedRequest.digestId, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").UPDATE)";

	public static final String IMPORT_SOURCE_FEED_FOR_DIGEST = 
			"hasPermission(#id, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").UPDATE)";
	
	public static final String UPDATE_SOURCE_FEED = 
			"hasPermission(#sourceFeedRequest, T(" + PERMISSION_CLASS + ").UPDATE) and " + 
			"hasPermission(#sourceFeedRequest.digestId, '" + DIGEST_CLASS + "', T(" + PERMISSION_CLASS + ").UPDATE)";
	
	public static final String DELETE_SOURCE_FEED = 
			"hasPermission(#id, '" + SOURCE_FEED_CLASS + "', T(" + PERMISSION_CLASS + ").DELETE)";
}
