import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from './core/auth/authentication.service';
import { PrincipalService } from './core/auth/principal.service';
import { Location } from '@angular/common';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.css']
})
export class AppComponent {

	constructor(
		public principal: PrincipalService,
		private auth: AuthenticationService,
		private router: Router,
		private location: Location) {

		router.navigate([this.getNextPath()]);
	}

	private getNextPath() {
		if (this.principal.isAuthenticated()) {
			let locationPath = this.location.path();
			return locationPath ? locationPath : 'digests/my';
		}
		else {
			return 'login';
		}
	}

	logout() {
		this.auth.logout().then(() => this.router.navigate(['login']));
	}

}
