package io.bottomfeeder.digest;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.RandomStringUtils;

import io.bottomfeeder.user.User;

/**
 * Digest represents an RSS or Atom feed built from aggregated content of one
 * or many source feeds.
 */
@Entity
@Table(name = "digest")
public class Digest {
	
	public static final int TITLE_MIN_SIZE = 1;
	public static final int TITLE_MAX_SIZE = 200;
	
	public static final int MAX_ENTRIES_MIN = 1;
	public static final int MAX_ENTRIES_MAX = 500;
	public static final int MAX_ENTRIES_DEFAULT = 20;
	
	public static final int EXTERNAL_ID_SIZE = 16;
	private static final String EXTERNAL_ID_CHARS_STRING = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final char[] EXTERNAL_ID_CHARS = EXTERNAL_ID_CHARS_STRING.toCharArray();
	public static final String EXTERNAL_ID_REGEX = "^[" + EXTERNAL_ID_CHARS_STRING + "]{" + EXTERNAL_ID_SIZE + "}$";
	
	public static final String VALIDATION_TITLE_NULL = "{validation.digest.title.null}";
	public static final String VALIDATION_TITLE_SIZE = "{validation.digest.title.size}";
	public static final String VALIDATION_EXTERNAL_ID_NULL = "{validation.digest.external-id.null}";
	public static final String VALIDATION_EXTERNAL_ID_REGEX = "{validation.digest.external-id.regex}";
	public static final String VALIDATION_EXTERNAL_ID_SIZE = "{validation.digest.external-id.size}";
	private static final String VALIDATION_CREATION_DATE_NULL = "{validation.digest.creation-date.null}";
	public static final String VALIDATION_MAX_ENTRIES_MIN = "{validation.digest.max-entries.min}";
	public static final String VALIDATION_MAX_ENTRIES_MAX = "{validation.digest.max-entries.max}";
	private static final String VALIDATION_OWNER_NULL = "{validation.digest.owner.null}";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = VALIDATION_TITLE_NULL)
	@Size(message = VALIDATION_TITLE_SIZE, min = TITLE_MIN_SIZE, max = TITLE_MAX_SIZE)
	@Column(length = TITLE_MAX_SIZE, nullable = false)
	private String title;
	
	@NotNull(message = VALIDATION_EXTERNAL_ID_NULL)
	@Pattern(message = VALIDATION_EXTERNAL_ID_REGEX, regexp = EXTERNAL_ID_REGEX)
	@Size(message = VALIDATION_EXTERNAL_ID_SIZE, min = EXTERNAL_ID_SIZE, max = EXTERNAL_ID_SIZE)
	@Column(name = "external_id", length = EXTERNAL_ID_SIZE, unique = true, nullable = false)
	private String externalId;
	
	@NotNull(message = VALIDATION_CREATION_DATE_NULL)
	@Column(name = "creation_date", nullable = false)
	private Instant creationDate = Instant.now();
	
	@Min(message = VALIDATION_MAX_ENTRIES_MIN, value = MAX_ENTRIES_MIN)
	@Max(message = VALIDATION_MAX_ENTRIES_MAX, value = MAX_ENTRIES_MAX)
	@Column(name = "max_entries")
	private int maxEntries = MAX_ENTRIES_DEFAULT;
	
	@Column(name = "is_private")
	private boolean isPrivate;

	@NotNull(message = VALIDATION_OWNER_NULL)
	@ManyToOne(optional = false)
	private User owner;
	
	
	public Digest() {}

	
	public Digest(String title, int maxEntries, boolean isPrivate, User owner, String externalId) {
		this.title = title;
		this.maxEntries = maxEntries;
		this.isPrivate = isPrivate;
		this.owner = owner;
		this.externalId = externalId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public static String createExternalId() {
		return RandomStringUtils.random(EXTERNAL_ID_SIZE, EXTERNAL_ID_CHARS);
	}

}
