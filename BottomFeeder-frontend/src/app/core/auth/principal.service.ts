import { Injectable } from '@angular/core';
import { LocalStorageService } from 'ngx-webstorage';
import { Account } from '../../account/account.model';

@Injectable({ providedIn: 'root' })
export class PrincipalService {

	private static STORAGE_KEY = "bf_account";

	constructor(private localStorage: LocalStorageService) { }

	setAccount(account: Account): void {
		this.localStorage.store(PrincipalService.STORAGE_KEY, account);
	}

	getAccount(): Account {
		return this.retrieveAccount();
	}

	clearAccount() {
		this.localStorage.clear(PrincipalService.STORAGE_KEY);
	}

	isAuthenticated(): boolean {
		return !!this.retrieveAccount();
	}

	isAdmin(): boolean {
		let account: Account = this.retrieveAccount();
		return account && account.role === 'ADMIN';
	}

	private retrieveAccount() {
		return this.localStorage.retrieve(PrincipalService.STORAGE_KEY);
	}
}
