import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountComponent } from './account/account.component';
import { LoginComponent } from './account/login.component';
import { SignupComponent } from './account/signup.component';
import { AppComponent } from './app.component';
import { AdminGuard } from './core/auth/admin.guard';
import { AuthenticatedGuard } from './core/auth/authenticated.guard';

const digestsModule = () => import('./digests/digests.module').then(m => m.DigestsModule);
const usersModule = () => import('./users/users.module').then(m => m.UsersModule);

const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'signup', component: SignupComponent },
	{ path: 'account', component: AccountComponent, canActivate: [AuthenticatedGuard] },
	{ path: 'digests', loadChildren: digestsModule, canActivate: [AuthenticatedGuard] },
	{ path: 'users', loadChildren: usersModule, canActivate: [AdminGuard] },
	{ path: '', component: AppComponent },
	{ path: '**', redirectTo: '' }
];

@NgModule({
	declarations: [],
	imports: [
		RouterModule.forRoot(routes, {useHash: true})
	],
	exports: [RouterModule]
})
export class AppRoutingModule { }
