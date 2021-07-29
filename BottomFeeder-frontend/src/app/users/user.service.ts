import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { NewUser, User } from './user.model';

@Injectable({
	providedIn: 'root'
})
export class UserService {
	private baseURL = `${environment.apiURL}/users`;

	constructor(private http: HttpClient) { }

	getAllUsers() {
		return this.http.get<User[]>(this.baseURL);
	}

	getUser(id: number) {
		return this.http.get<User>(`${this.baseURL}/${id}`);
	}

	createUser(newUser: NewUser) {
		return this.http.post<User>(this.baseURL, newUser);
	}

	updateUser(user: User) {
		return this.http.put<User>(this.baseURL, user);
	}

	deleteUser(id: number) {
		return this.http.delete<void>(`${this.baseURL}/${id}`);
	}

	exportUsers() {
		return this.http.get(`${this.baseURL}/export`, { responseType: 'blob' });
	}

}
