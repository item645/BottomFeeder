import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PrincipalService } from '../core/auth/principal.service';
import { AccountService } from './account.service';

@Component({
	selector: 'app-account',
	templateUrl: './account.component.html',
	styleUrls: ['./account.component.css']
})
export class AccountComponent implements OnInit {

	form: FormGroup;
	loading = false;
	submitted = false;

	constructor(
		private formBuilder: FormBuilder,
		private router: Router,
		private accountService: AccountService,
		private principal: PrincipalService
	) { }

	ngOnInit(): void {
		this.form = this.formBuilder.group({
			currentPassword: ['', Validators.required],
			newPassword: ['', [Validators.required, Validators.minLength(5)]]
		});
	}

	get f() {
		return this.form.controls;
	}

	changePassword() {
		this.submitted = true;
		if (this.form.invalid) {
			return;
		}

		this.loading = true;
		this.accountService
			.changePassword(this.f.currentPassword.value, this.f.newPassword.value)
			.subscribe(
				() => {
					this.principal.clearAccount();
					this.router.navigate(['login']);
				},
				() => this.loading = false
			);
	}
	
}
