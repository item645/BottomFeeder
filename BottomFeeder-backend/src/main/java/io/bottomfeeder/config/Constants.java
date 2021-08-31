package io.bottomfeeder.config;

/**
 * General-purpose constants. 
 */
public final class Constants {

	private Constants() {}
	
	public static final String API_URL_BASE = "/api";
	public static final String API_URL_DIGESTS = API_URL_BASE + "/digests";
	public static final String API_URL_SOURCE_FEEDS = API_URL_BASE + "/feeds";
	public static final String API_URL_ENTRY_FILTERS = API_URL_BASE + "/filters";
	public static final String API_URL_USERS = API_URL_BASE + "/users";
	
	public static final String DIGEST_FEED_URL = "/digest";
	
	public static final String ANONYMOUS_PRINCIPAL = "*anonymous*";
}
