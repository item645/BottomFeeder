import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserListComponent } from './user-list.component';
import { UserComponent } from './user.component';

const routes: Routes = [
	{ path: '', component: UserListComponent },
	{ path: 'add', component: UserComponent },
	{ path: 'edit/:id', component: UserComponent }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class UsersRoutingModule { }
