package io.bottomfeeder.sourcefeed;

import java.time.Instant;

/**
 * Groups together data related to source feed content.
 */
public record SourceFeedContent(String title, String content, Instant updateDate) {
}
