package io.bottomfeeder.digest.feed;

/**
 * Enumerates supported output feed formats for digest.
 */
public enum DigestFeedFormat {

	ATOM_1_0 ("atom_1.0", "application/atom+xml", "atom"),
	RSS_2_0  ("rss_2.0",  "application/rss+xml",  "rss");
	
	private final String type;
	private final String contentType;
	private final String extension;

	DigestFeedFormat(String type, String contentType, String extension) {
		this.type = type;
		this.contentType = contentType;
		this.extension = extension;
	}
	
	public String type() {
		return type;
	}

	public String contentType() {
		return contentType;
	}

	public String extension() {
		return extension;
	}

}
