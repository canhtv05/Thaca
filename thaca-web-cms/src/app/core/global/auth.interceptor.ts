import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { currentLang, isLoading } from '../stores/app.store';
import { SKIP_LOADING } from './http-context';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
): Observable<HttpEvent<any>> => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const toastrService = inject(ToastrService);

  const skipLoading = req.context.get(SKIP_LOADING);

  const authReq = req.clone({
    withCredentials: true,
  });

  if (!skipLoading) {
    isLoading.set(true);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const lang = currentLang() || 'vi';
      let title = lang === 'vi' ? 'Lỗi hệ thống' : 'System error';
      let message =
        lang === 'vi' ? 'Đã xảy ra lỗi không xác định' : 'Unknown internal server error';

      const backendError = error?.error?.body?.data;
      if (backendError) {
        if (lang === 'vi') {
          title = backendError.titleVi || title;
          message = backendError.messageVi || message;
        } else {
          title = backendError.titleEn || title;
          message = backendError.messageEn || message;
        }
      }

      switch (error.status) {
        case 401:
          authService.logout();
          router.navigateByUrl('/login');
          toastrService.error(message, title);
          return throwError(() => error);
        case 403:
          router.navigate(['/403']);
          toastrService.warning(message, title);
          break;
        case 0:
          title = lang === 'vi' ? 'Lỗi mạng' : 'Network error';
          message = lang === 'vi' ? 'Không thể kết nối đến máy chủ' : 'Cannot connect to server';
          toastrService.error(message, title);
          break;
        default:
          if (error.status >= 500) {
            toastrService.error(message, title);
          } else {
            toastrService.warning(message, title);
          }
          break;
      }
      return throwError(() => error);
    }),
    finalize(() => {
      if (!skipLoading) {
        isLoading.set(false);
      }
    }),
  );
};
