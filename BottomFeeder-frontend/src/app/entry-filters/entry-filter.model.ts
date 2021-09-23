export class EntryFilter {
	constructor(
		public id: number,
		public ordinal: number,
		public element: Element,
		public condition: Condition,
		public value: string,
		public connective: Connective
	) {}
}

export enum Element {
	AUTHOR = 'AUTHOR',
	CATEGORIES = 'CATEGORIES',
	CONTENT = 'CONTENT',
	LINK = 'LINK',
	PUBLISH_DATE = 'PUBLISH_DATE',
	TITLE = 'TITLE',
	UPDATE_DATE = 'UPDATE_DATE'
}

export enum Condition {
	CONTAINS = 'CONTAINS',
	DOES_NOT_CONTAIN = 'DOES_NOT_CONTAIN',
	EQUALS = 'EQUALS',
	DOES_NOT_EQUAL = 'DOES_NOT_EQUAL',
	LESS_THAN = 'LESS_THAN',
	MORE_THAN = 'MORE_THAN'
}

export enum Connective {
	AND = 'AND',
	OR = 'OR'
}