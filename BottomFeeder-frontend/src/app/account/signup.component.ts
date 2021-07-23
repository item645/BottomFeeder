import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from './account.service';

@Component({
	selector: 'app-signup',
	templateUrl: './signup.component.html',
	styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
	
	form: FormGroup;
	loading = false;
	submitted = false;

	constructor(
		private formBuilder: FormBuilder,
		private router: Router,
		private accountService: AccountService
	) { }

	ngOnInit(): void {
		this.form = this.formBuilder.group({
			login: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
			password: ['', [Validators.required, Validators.minLength(5)]]
		});
	}

	get f() {
		return this.form.controls;
	}

	signup() {
		this.submitted = true;
		if (this.form.invalid) {
			return;
		}

		this.loading = true;
		this.accountService
			.signup(this.f.login.value, this.f.password.value)
			.subscribe(
				() => this.router.navigate(['/']),
				() => this.loading = false
			);
	}
	
}
