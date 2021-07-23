import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';

import { PrincipalService } from './principal.service';

@Injectable({ providedIn: 'root' })
export class AuthenticatedGuard implements CanActivate {

	constructor(private principal: PrincipalService) {}

	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {
		return this.principal.isAuthenticated();
	}

}
