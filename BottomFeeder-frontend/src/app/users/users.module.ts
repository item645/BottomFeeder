import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserComponent } from './user.component';
import { UserListComponent } from './user-list.component';
import { UsersRoutingModule } from './users-routing.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
	declarations: [
		UserListComponent,
		UserComponent
	],
	imports: [
		CommonModule,
		UsersRoutingModule,
		ReactiveFormsModule
	]
})
export class UsersModule { }
