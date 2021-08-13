package io.bottomfeeder.sourcefeed;

import java.net.URI;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import io.bottomfeeder.digest.Digest;

/**
 * Represents a source feed, that is, a single RSS or Atom feed whose content is used to 
 * build a {@code Digest}.
 */
@Entity
@Table(name = "source_feed")
public class SourceFeed {
	
	public static final int SOURCE_MIN_SIZE = 1;
	public static final int SOURCE_MAX_SIZE = 400;
	
	public static final int TITLE_MAX_SIZE = 300;
	
	public static final int CONTENT_UPDATE_INTERVAL_MIN = 10;
	public static final int CONTENT_UPDATE_INTERVAL_MAX = 1440;
	private static final int CONTENT_UPDATE_INTERVAL_DEFAULT = 60;
	
	public static final int MAX_ENTRIES_MIN = 0;
	public static final int MAX_ENTRIES_MAX = Integer.MAX_VALUE;
	private static final int MAX_ENTRIES_DEFAULT = MAX_ENTRIES_MIN;
	
	public static final String VALIDATION_SOURCE_NULL = "{validation.source-feed.source.null}";
	public static final String VALIDATION_SOURCE_SIZE = "{validation.source-feed.source.size}";
	private static final String VALIDATION_SOURCE_URL = "{validation.source-feed.source.url}";
	public static final String VALIDATION_TITLE_SIZE = "{validation.source-feed.title.size}";
	private static final String VALIDATION_CREATION_DATE_NULL = "{validation.source-feed.creation-date.null}";
	public static final String VALIDATION_CONTENT_UPDATE_INTERVAL_MIN = "{validation.source-feed.content-update-interval.min}";
	public static final String VALIDATION_CONTENT_UPDATE_INTERVAL_MAX = "{validation.source-feed.content-update-interval.max}";
	public static final String VALIDATION_MAX_ENTRIES_MIN = "{validation.source-feed.max-entries.min}";
	public static final String VALIDATION_MAX_ENTRIES_MAX = "{validation.source-feed.max-entries.max}";
	private static final String VALIDATION_DIGEST_NULL = "{validation.source-feed.digest.null}";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = VALIDATION_SOURCE_NULL)
	@Size(message = VALIDATION_SOURCE_SIZE, min = SOURCE_MIN_SIZE, max = SOURCE_MAX_SIZE)
	@URL(message = VALIDATION_SOURCE_URL)
	@Column(length = SOURCE_MAX_SIZE, nullable = false)
	private String source;

	@Size(message = VALIDATION_TITLE_SIZE, max = TITLE_MAX_SIZE)
	@Column(length = TITLE_MAX_SIZE)
	private String title;
	
	@NotNull(message = VALIDATION_CREATION_DATE_NULL)
	@Column(name = "creation_date", nullable = false)
	private Instant creationDate = Instant.now();
	
	@Column(name = "content_update_date")
	private Instant contentUpdateDate;
	
	@Min(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MIN, value = CONTENT_UPDATE_INTERVAL_MIN)
	@Max(message = VALIDATION_CONTENT_UPDATE_INTERVAL_MAX, value = CONTENT_UPDATE_INTERVAL_MAX)
	@Column(name = "content_update_interval")
	private int contentUpdateInterval = CONTENT_UPDATE_INTERVAL_DEFAULT;
	
	@Min(message = VALIDATION_MAX_ENTRIES_MIN, value = MAX_ENTRIES_MIN)
	@Max(message = VALIDATION_MAX_ENTRIES_MAX, value = MAX_ENTRIES_MAX)
	@Column(name = "max_entries", nullable = false)
	private int maxEntries = MAX_ENTRIES_DEFAULT;
	
	@NotNull(message = VALIDATION_DIGEST_NULL)
	@ManyToOne(optional = false)
	private Digest digest;


	public SourceFeed() {}

	public SourceFeed(String source, int contentUpdateInterval, int maxEntries, Digest digest) {
		this.source = source;
		this.contentUpdateInterval = contentUpdateInterval;
		this.maxEntries = maxEntries;
		this.digest = digest;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public Instant getContentUpdateDate() {
		return contentUpdateDate;
	}

	public void setContentUpdateDate(Instant contentUpdateDate) {
		this.contentUpdateDate = contentUpdateDate;
	}

	public int getContentUpdateInterval() {
		return contentUpdateInterval;
	}

	public void setContentUpdateInterval(int contentUpdateInterval) {
		this.contentUpdateInterval = contentUpdateInterval;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public Digest getDigest() {
		return digest;
	}

	public void setDigest(Digest digest) {
		this.digest = digest;
	}
	
	@Transient
	public URI getURI() {
		return URI.create(source);
	}
	
	@Transient
	public String getTruncatedSource() {
		return StringUtils.abbreviate(source, 80);
	}
	
	@Transient
	public void setAbbreviatedTitle(String title) {
		this.title = StringUtils.abbreviate(StringUtils.trimToNull(title), SourceFeed.TITLE_MAX_SIZE);
	}
	
}
