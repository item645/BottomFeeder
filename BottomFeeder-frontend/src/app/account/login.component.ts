import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/core/auth/authentication.service';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

	form: FormGroup;
	loading = false;
	submitted = false;

	constructor(
		private formBuilder: FormBuilder,
		private auth: AuthenticationService,
		private router: Router
	) { }

	ngOnInit(): void {
		this.form = this.formBuilder.group({
			login: ['', Validators.required],
			password: ['', Validators.required]
		});
	}


	get f() {
		return this.form.controls;
	}

	authenticate() {
		this.submitted = true;
		if (this.form.invalid) {
			return;
		}

		this.loading = true;
		this.auth
			.authenticate(this.f.login.value, this.f.password.value)
			.then(
				() => this.router.navigate(['/']),
				() => this.loading = false
			);
	}
}
