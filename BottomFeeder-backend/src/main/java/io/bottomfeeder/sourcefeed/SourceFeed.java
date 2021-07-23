package io.bottomfeeder.sourcefeed;

import java.net.URI;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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
	public static final int CONTENT_UPDATE_INTERVAL_DEFAULT = 60;
	
	public static final String VALIDATION_SOURCE_NULL = "{validation.source-feed.source.null}";
	public static final String VALIDATION_SOURCE_SIZE = "{validation.source-feed.source.size}";
	public static final String VALIDATION_SOURCE_URL = "{validation.source-feed.source.url}";
	public static final String VALIDATION_TITLE_SIZE = "{validation.source-feed.title.size}";
	public static final String VALIDATION_CREATION_DATE_NULL = "{validation.source-feed.creation-date.null}";
	public static final String VALIDATION_CONTENT_UPDATE_INTERVAL_MIN = "{validation.source-feed.content-update-interval.min}";
	public static final String VALIDATION_CONTENT_UPDATE_INTERVAL_MAX = "{validation.source-feed.content-update-interval.max}";
	public static final String VALIDATION_DIGEST_NULL = "{validation.source-feed.digest.null}";
	
	
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
	private int contentUpdateInterval;
	
	@Lob
	private String content;
	
	@NotNull(message = VALIDATION_DIGEST_NULL)
	@ManyToOne(optional = false)
	private Digest digest;


	public SourceFeed() {}

	public SourceFeed(String source, int contentUpdateInterval, Digest digest) {
		this.source = source;
		this.contentUpdateInterval = contentUpdateInterval;
		this.digest = digest;
	}
	
	public SourceFeed(Long id, String source, String title, Instant creationDate, Instant contentUpdateDate, 
			int contentUpdateInterval, Digest digest) {
		this.id = id;
		this.source = source;
		this.title = title;
		this.creationDate = creationDate;
		this.contentUpdateDate = contentUpdateDate;
		this.contentUpdateInterval = contentUpdateInterval;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Digest getDigest() {
		return digest;
	}

	public void setDigest(Digest digest) {
		this.digest = digest;
	}
	
	@Transient
	public void setUpdatedContent(SourceFeedContent updatedContent) {
		if (updatedContent != null) {
			title = updatedContent.title();
			content = updatedContent.content();
			contentUpdateDate = updatedContent.updateDate();
		}
		else {
			title = null;
			content = null;
			contentUpdateDate = null;
		}
	}
	
	@Transient
	public URI getURI() {
		return URI.create(source);
	}
	
	@Transient
	public String getTruncatedSource() {
		return StringUtils.abbreviate(source, 80);
	}
}
