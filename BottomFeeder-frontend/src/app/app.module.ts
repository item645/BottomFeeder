import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AuthenticationInterceptor } from './core/auth/authentication.interceptor';
import { AppRoutingModule } from './app-routing.module';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { LoginComponent } from './account/login.component';
import { AccountComponent } from './account/account.component';
import { ServerResponseInterceptor } from './core/server-response.interceptor';
import { ToastrModule } from 'ngx-toastr';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SignupComponent } from './account/signup.component';

@NgModule({
	declarations: [
		AppComponent,
		LoginComponent,
		AccountComponent,
		SignupComponent,
	],
	imports: [
		BrowserModule,
		FormsModule,
		ReactiveFormsModule,
		HttpClientModule,
		AppRoutingModule,
		BrowserAnimationsModule,
		NgxWebstorageModule.forRoot(),
		ToastrModule.forRoot()
	],
	providers: [
		{ provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
		{ provide: HTTP_INTERCEPTORS, useClass: ServerResponseInterceptor, multi: true }
	],
	bootstrap: [AppComponent]
})
export class AppModule { }
