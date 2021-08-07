package io.bottomfeeder.sourcefeed.entry;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.bottomfeeder.sourcefeed.SourceFeed;

/**
 * Represents an individual entry of source feed's content.
 * 
 * The actual entry content is stored as a dummy "feed" that contains only this one entry.
 * This is due to the fact that ROME library does not provide an API for parsing individual
 * entries outside of feed's context.
 * 
 * The date property contains date/time of this entry which is usually published date, or updated date,
 * if published date is not specified.
 */
@Entity
@Table(name = "source_feed_entry")
public class SourceFeedEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false)
	private Instant date;
	
	@NotNull
	@Lob
	@Column(nullable = false)
	private byte[] content;
	
	@NotNull
	@ManyToOne(optional = false)
	private SourceFeed sourceFeed;

	
	public SourceFeedEntry() {}

	public SourceFeedEntry(Instant date, byte[] content, SourceFeed sourceFeed) {
		this.date = date;
		this.content = content;
		this.sourceFeed = sourceFeed;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public SourceFeed getSourceFeed() {
		return sourceFeed;
	}

	public void setSourceFeed(SourceFeed sourceFeed) {
		this.sourceFeed = sourceFeed;
	}
	
}
