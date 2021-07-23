import { Observable, throwError } from 'rxjs';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { tap, map, catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { PrincipalService } from './auth/principal.service';

@Injectable({ providedIn: 'root' })
export class ServerResponseInterceptor implements HttpInterceptor {

	constructor(
		private toastr: ToastrService, 
		private router: Router,
		private principal: PrincipalService
	) { }

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return next.handle(request).pipe(
			tap(event => this.notifySuccess(event)),
			catchError(err => this.handleError(err)),
			map(event => this.getResponseData(event))
		);
	}

	private getResponseData(event: HttpEvent<any>) {
		if (event instanceof HttpResponse && event.body && event.body.data)
			return event.clone({ body: event.body.data });
		else
			return event;
	}

	private notifySuccess(event: HttpEvent<any>) {
		if (event instanceof HttpResponse) {
			let response = event.body;
			if (response && response.httpStatus >= 200 && response.httpStatus < 300 && response.message) {
				this.toastr.success(response.message, '', {
					timeOut: 3500,
					positionClass: 'toast-bottom-left'
				});
				console.log(response.message);
			}
		}
	}

	private handleError(err: any) {
		if (err instanceof HttpErrorResponse) {
			let title: string = '';
			let message: string = err.message;

			if (err.error && err.error.data) {
				let description = err.error.data.description;
				let details = err.error.data.details;
				if (Array.isArray(details) && details.length > 0) {
					title = description;
					message = details.join('; ');
				}
				else {
					message = description;
				}
			}
			else {
				message = err.error.message || message;
			}

			this.toastr.error(message, title, {
				timeOut: 10000,
				positionClass: 'toast-bottom-left'
			});

			console.error(title ? `${title}: ${message}` : message);

			if (err.status === 401) {
				this.principal.clearAccount();
				this.router.navigate(['login']);
			}
		}
		return throwError(err);
	}
}
