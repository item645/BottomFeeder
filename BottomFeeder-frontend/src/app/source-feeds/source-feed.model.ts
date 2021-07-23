export class SourceFeed {
	constructor(
		public id: number,
		public digestId: number,
		public source: string,
		public title: string,
		public creationDate: Date,
		public contentUpdateDate: Date,
		public contentUpdateInterval: number,
		public updateContent: boolean
	) { }
}

export class NewSourceFeed {
	constructor(
		public digestId: number,
		public source: string,
		public contentUpdateInterval: number,
		public updateContent: boolean
	) { }
}