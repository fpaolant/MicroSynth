import { Injectable } from '@angular/core';
import {
  HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { AuthService, TokenResponse } from './auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    let clonedReq = req;

    if (token && req.headers.has('Use-Auth')) {
      clonedReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
        headers: req.headers.delete('Use-Auth')
      });
    }

    return next.handle(clonedReq).pipe(
      catchError(error => {
        if (
          error instanceof HttpErrorResponse &&
          error.status === 401 &&
          !clonedReq.url.includes('/refresh') // evita loop infinito
        ) {
          return this.handle401Error(clonedReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.authService.refreshToken().pipe(
        switchMap((response: TokenResponse) => {
          this.isRefreshing = false;
          const newToken = response.token;
          this.refreshTokenSubject.next(newToken);

          const clonedReq = req.clone({
            setHeaders: { Authorization: `Bearer ${newToken}` }
          });

          return next.handle(clonedReq);
        }),
        catchError(err => {
          this.isRefreshing = false;
          this.authService.logout();
          this.router.navigate(['/login']);
          return throwError(() => err);
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => {
          const clonedReq = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
          });
          return next.handle(clonedReq);
        })
      );
    }
  }
}
