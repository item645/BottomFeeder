import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { User } from './user.model';
import { UserService } from './user.service';
import { Location } from '@angular/common';

@Component({
	selector: 'app-user',
	templateUrl: './user.component.html',
	styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {
	private id: number;
	form: FormGroup;
	isNewUser: boolean;
	loading = false;
	submitted = false;

	constructor(
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		private userService: UserService,
		public location: Location,
	) { }

	ngOnInit() {
		this.id = this.route.snapshot.params['id'];
		this.isNewUser = !this.id;

		let controls = {
			login: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
			role: ['USER', Validators.required],
		}
		if (this.isNewUser)
			controls = Object.assign(controls, { password: ['', [Validators.required, Validators.minLength(5)]] })
		this.form = this.formBuilder.group(controls);

		if (!this.isNewUser) {
			this.userService
				.getUser(this.id)
				.subscribe(
					user => {
						this.f.login.setValue(user.login);
						this.f.role.setValue(user.role);
					}
				);
		}
	}

	get f() { return this.form.controls; }

	save() {
		this.submitted = true;
		if (this.form.invalid) {
			return;
		}

		this.loading = true;
		if (this.isNewUser)
			this.createUser();
		else
			this.updateUser();
	}

	private createUser() {
		this.userService
			.createUser(this.form.value)
			.subscribe(
				() => this.router.navigate(['..'], { relativeTo: this.route }),
				() => this.loading = false
			);
	}

	private updateUser() {
		let user: User = Object.assign(this.form.value, {id: this.id});
		this.userService
			.updateUser(user)
			.subscribe(
				() => this.router.navigate(['../../'], { relativeTo: this.route }),
				() => this.loading = false
			);
	}
	
}
