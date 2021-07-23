import { Role } from "../core/auth/role.model";

export class User {
	constructor(
		public id: number,
		public login: string,
		public role: Role
	) { }
}

export class NewUser {
	constructor(
		public login: string,
		public password: string,
		public role: Role
	) { }
}