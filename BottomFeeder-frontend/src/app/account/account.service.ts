import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Account } from './account.model';

@Injectable({
	providedIn: 'root'
})
export class AccountService {

	constructor(private http: HttpClient) { }

	get() {
		return this.http.get<Account>(`${environment.apiURL}/account`);
	}

	signup(login: string, password: string) {
		return this.http.post<void>(`${environment.apiURL}/signup`, { login: login, password: password });
	}

	changePassword(currentPassword: string, newPassword: string) {
		return this.http.put<void>(`${environment.apiURL}/account/change-password`, 
			{ currentPassword: currentPassword, newPassword: newPassword });
	}
	
}
