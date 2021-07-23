import { Role } from "../core/auth/role.model";

export class Account {
	constructor(
		public login: string,
		public role: Role
	) { }
}
