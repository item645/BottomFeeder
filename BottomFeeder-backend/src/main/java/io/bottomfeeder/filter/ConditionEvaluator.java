package io.bottomfeeder.filter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a function that evaluates condition defined by filter and produces boolean result.
 * The function accepts two arguments: a value of arbitrary type obtained from feed entry element,
 * and string value specified by entry filter.
 * 
 * This interface also provides static factory methods to create condition evaluators for specific
 * combinations of condition and data type; such evaluators treat their function arguments appropriately,
 * converting them to target data type when needed.  
 */
@FunctionalInterface
interface ConditionEvaluator {
	
	boolean evaluate(Object entryElementValue, String filterValue);
	
	
	default ConditionEvaluator negate() {
		return (entryElementValue, filterValue) -> !evaluate(entryElementValue, filterValue);
	}


	static ConditionEvaluator stringContains() {
		return (entryElementValue, filterValue) -> StringUtils.containsIgnoreCase((String)entryElementValue, filterValue);
	}
	
	
	static ConditionEvaluator stringDoesNotContain() {
		return stringContains().negate();
	}
	
	
	static ConditionEvaluator stringEquals() {
		return (entryElementValue, filterValue) -> StringUtils.equalsIgnoreCase((String)entryElementValue, filterValue);
	}

	
	static ConditionEvaluator stringDoesNotEqual() {
		return stringEquals().negate();
	}
	
	
	static ConditionEvaluator stringListContains() {
		return (entryElementValue, filterValue) -> {
			@SuppressWarnings("unchecked")
			var list = (List<String>)entryElementValue;
			return list != null && list.stream().anyMatch(value -> StringUtils.equalsIgnoreCase(value, filterValue));
		};
	}
	
	
	static ConditionEvaluator stringListDoesNotContain() {
		return stringListContains().negate();
	}
	
	
	static ConditionEvaluator dateTimeEquals() {
		return (entryElementValue, filterValue) -> 
			DateTimeUtils.isEqual(DateTimeUtils.castAsInstant(entryElementValue), DateTimeUtils.parseAsInstant(filterValue));
	}
	
	
	static ConditionEvaluator dateTimeDoesNotEqual() {
		return dateTimeEquals().negate();
	}
	
	
	static ConditionEvaluator dateTimeLessThan() {
		return (entryElementValue, filterValue) -> 
			DateTimeUtils.isBefore(DateTimeUtils.castAsInstant(entryElementValue), DateTimeUtils.parseAsInstant(filterValue));
	}
	
	
	static ConditionEvaluator dateTimeMoreThan() {
		return (entryElementValue, filterValue) -> 
			DateTimeUtils.isAfter(DateTimeUtils.castAsInstant(entryElementValue), DateTimeUtils.parseAsInstant(filterValue));
	}
	
}
