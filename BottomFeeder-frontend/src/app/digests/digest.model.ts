export class Digest {
	constructor(
		public id: number,
		public externalId: string,
		public rssLink: string,
		public atomLink: string,
		public ownerLogin: string,
		public title: string,
		public creationDate: Date,
		public maxItems: number,
		public isPrivate: boolean
	) { }
}

export class NewDigest {
	constructor(
		public title: string,
		public maxItems: number,
		public isPrivate: boolean
	) { }
}

export class DigestTitle {
	constructor(
		public id: number,
		public ownerLogin: string,
		public title: string,
	) { }
}