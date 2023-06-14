import { TokenService } from './../services/token.service';
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HTTP_INTERCEPTORS,
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class GameInterceptor implements HttpInterceptor {
  constructor(private tokenService: TokenService) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    let authRequest = request;
    const token = this.tokenService.getToken();
    if (token !== null)
      authRequest = authRequest.clone({
        headers: request.headers.set('Authorization', 'Bearer ' + token),
      });

    return next.handle(request);
  }
}

export const gameInterceptor = [
  { provide: HTTP_INTERCEPTORS, useClass: GameInterceptor, multi: true },
];
