package io.bottomfeeder.filter;

import static io.bottomfeeder.filter.Condition.*;
import static io.bottomfeeder.filter.ElementDataType.*;

/**
 * Enumerates valid combinations of element data type and condition.
 * Each enum constant maps data type to the condition applicable for that type and provides 
 * appropriate {@code ConditionEvaluator}.
 */
enum DataTypeCondition {

	STRING_CONTAINS              (STRING,      CONTAINS,         ConditionEvaluator.stringContains()),
	STRING_DOES_NOT_CONTAIN      (STRING,      DOES_NOT_CONTAIN, ConditionEvaluator.stringDoesNotContain()),
	STRING_EQUALS                (STRING,      EQUALS,           ConditionEvaluator.stringEquals()),
	STRING_DOES_NOT_EQUAL        (STRING,      DOES_NOT_EQUAL,   ConditionEvaluator.stringDoesNotEqual()),
	STRING_LIST_CONTAINS         (STRING_LIST, CONTAINS,         ConditionEvaluator.stringListContains()),
	STRING_LIST_DOES_NOT_CONTAIN (STRING_LIST, DOES_NOT_CONTAIN, ConditionEvaluator.stringListDoesNotContain()),
	DATE_TIME_EQUALS             (DATE_TIME,   EQUALS,           ConditionEvaluator.dateTimeEquals()),
	DATE_TIME_DOES_NOT_EQUAL     (DATE_TIME,   DOES_NOT_EQUAL,   ConditionEvaluator.dateTimeDoesNotEqual()),
	DATE_TIME_LESS_THAN          (DATE_TIME,   LESS_THAN,        ConditionEvaluator.dateTimeLessThan()),
	DATE_TIME_MORE_THAN          (DATE_TIME,   MORE_THAN,        ConditionEvaluator.dateTimeMoreThan());
	
	
	private final ElementDataType dataType;
	private final Condition condition;
	private final ConditionEvaluator conditionEvaluator;
	
	
	DataTypeCondition(ElementDataType dataType, Condition condition, ConditionEvaluator conditionEvaluator) {
		this.dataType = dataType;
		this.condition = condition;
		this.conditionEvaluator = conditionEvaluator;
	}
	
	
	ConditionEvaluator conditionEvaluator() {
		return conditionEvaluator;
	}

	
	static DataTypeCondition of(ElementDataType dataType, Condition condition) {
		for (var dataTypeCondition : values()) {
			if (dataTypeCondition.dataType == dataType && dataTypeCondition.condition == condition)
				return dataTypeCondition;
		}
		throw new IllegalArgumentException(String.format("Invalid condition %s for data type %s", condition, dataType));
	}
	

	static boolean isValid(ElementDataType dataType, Condition condition) {
		try {
			of(dataType, condition);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
	
}
