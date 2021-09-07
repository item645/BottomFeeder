package io.bottomfeeder.filter;

import static io.bottomfeeder.filter.Condition.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enumerates supported data types for entry element and specifies conditions applicable 
 * to given data type.
 */
enum ElementDataType {

	STRING (CONTAINS, DOES_NOT_CONTAIN, EQUALS, DOES_NOT_EQUAL),
	STRING_LIST (CONTAINS, DOES_NOT_CONTAIN),
	DATE_TIME (EQUALS, DOES_NOT_EQUAL, LESS_THAN, MORE_THAN);
	
	private final Set<Condition> supportedConditions;

	ElementDataType(Condition... supportedConditions) {
		this.supportedConditions = EnumSet.copyOf(Arrays.asList(supportedConditions));
	}

	boolean supportsCondition(Condition condition) {
		return supportedConditions.contains(condition);
	}
	
}
