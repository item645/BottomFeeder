package io.bottomfeeder.filter;

import static io.bottomfeeder.filter.ElementDataType.*;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;

/**
 * Enumerates supported generic elements for feed entry, specifies their data types and provides 
 * constant-specific implementations of accessor that allows to obtain corresponding value
 * from supplied SyndEntry instance.
 */
public enum Element {

	AUTHOR (STRING) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getAuthor();
		}
	},
	CATEGORIES (STRING_LIST) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getCategories().stream().map(SyndCategory::getName).collect(Collectors.toList());
		}
	},
	CONTENT (STRING) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			String value = null;
			
			var description = syndEntry.getDescription();
			if (description != null)
				value = description.getValue();
			
			if (value == null) {
				value = syndEntry.getContents().stream()
						.map(SyndContent::getValue)
						.filter(Objects::nonNull)
						.collect(Collectors.joining());
			}
			
			return StringUtils.trimToEmpty(value);
		}
	},
	LINK (STRING) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getLink();
		}
	},
	PUBLISH_DATE (DATE_TIME) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getPublishedDate();
		}
	},
	TITLE (STRING) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getTitle();
		}
	},
	UPDATE_DATE (DATE_TIME) {
		@Override
		Object readValue(SyndEntry syndEntry) {
			return syndEntry.getUpdatedDate();
		}
	};
	
	
	private final ElementDataType dataType;
	
	
	Element(ElementDataType dataType) {
		this.dataType = dataType;
	}

	
	ElementDataType dataType() {
		return dataType;
	}
	
	
	abstract Object readValue(SyndEntry syndEntry);

}
