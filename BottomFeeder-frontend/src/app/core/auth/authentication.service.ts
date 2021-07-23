import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { PrincipalService } from './principal.service';
import 'rxjs/add/operator/map';
import { Account } from '../../account/account.model';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {

	constructor(private http: HttpClient, private principal: PrincipalService) { }

	authenticate(login: string, password: string): Promise<Account> {
		let credentials = { login: login, password: password };
        return new Promise((resolve, reject) => {
			this.http
				.post<Account>(`${environment.apiURL}/authenticate`, credentials)
				.subscribe(
					account => {
						this.principal.setAccount(account);
						resolve(account);
					},
					error => reject(error));
        });
	}


	logout(): Promise<any> {
		return new Promise((resolve, reject) => {
			this.http
				.post(`${environment.apiURL}/logout`, null)
				.subscribe(
					() => {
						this.principal.clearAccount();
						resolve('Logged out');
					},
					error => reject(error)
				);
		});
	}

}
