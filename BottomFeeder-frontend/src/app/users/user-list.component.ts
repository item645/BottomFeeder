import { Component, OnInit } from '@angular/core';
import { PrincipalService } from '../core/auth/principal.service';
import { User } from './user.model';
import { UserService } from './user.service';

@Component({
	selector: 'app-user-list',
	templateUrl: './user-list.component.html',
	styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
	users: User[] = [];

	constructor(private userService: UserService, private principal: PrincipalService) { }

	ngOnInit() {
		this.userService.getAllUsers().subscribe(users => this.users = users);
	}

	deleteUser(id: number) {
		this.userService.deleteUser(id).subscribe(() => this.users = this.users.filter(u => u.id !== id));
	}

	isCurrentUser(login: string) {
		let account = this.principal.getAccount();
		return account && login === account.login;
	}
}
