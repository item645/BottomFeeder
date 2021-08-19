import { Component, OnInit } from '@angular/core';
import { PrincipalService } from '../core/auth/principal.service';
import { User } from './user.model';
import { UserService } from './user.service';
import { fileDialog } from 'file-select-dialog'
import { saveAs } from "file-saver";

@Component({
	selector: 'app-user-list',
	templateUrl: './user-list.component.html',
	styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
	users: User[] = [];

	constructor(private userService: UserService, private principal: PrincipalService) { }

	ngOnInit() {
		this.loadUsers();
	}

	deleteUser(id: number) {
		this.userService.deleteUser(id).subscribe(() => this.users = this.users.filter(u => u.id !== id));
	}

	importUsers() {
		fileDialog({accept: '.json', strict: true}).then(file => {
			this.userService
				.importUsers(file)
				.subscribe(() => this.loadUsers());
		});
	}

	exportUsers() {
		this.userService.exportUsers().subscribe(usersData => saveAs(usersData, 'users_data.json'));
	}

	isCurrentUser(login: string) {
		let account = this.principal.getAccount();
		return account && login === account.login;
	}

	private loadUsers() {
		this.userService.getAllUsers().subscribe(users => this.users = users);
	}

}
