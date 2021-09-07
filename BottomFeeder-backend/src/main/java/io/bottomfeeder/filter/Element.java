package io.bottomfeeder.filter;

import static io.bottomfeeder.filter.ElementDataType.*;

/**
 * Enumerates supported generic elements for feed entry and specifies their data types.
 */
public enum Element {

	AUTHOR (STRING),
	CATEGORIES (STRING_LIST),
	CONTENT (STRING),
	LINK (STRING),
	PUBLISH_DATE (DATE_TIME),
	TITLE (STRING),
	UPDATE_DATE (DATE_TIME);
	
	private final ElementDataType dataType;
	
	Element(ElementDataType dataType) {
		this.dataType = dataType;
	}

	ElementDataType dataType() {
		return dataType;
	}
	
}
