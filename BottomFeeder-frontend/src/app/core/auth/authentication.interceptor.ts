import { Observable } from 'rxjs';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';

import { environment } from 'src/environments/environment';
import { Injectable } from "@angular/core";

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (!request || !request.url || (/^https?/.test(request.url) && !(request.url.startsWith(environment.apiURL))))
			return next.handle(request);
		else
			return next.handle(request.clone({ withCredentials: true }));
	}
	
}
