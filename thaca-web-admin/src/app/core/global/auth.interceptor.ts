import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { from, Observable, throwError } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';
import { AuthService } from '../../pages/auth/auth.service';
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

  const skipLoading = req.context.get(SKIP_LOADING) || req.url.includes('/assets/');

  const authReq = req.clone({
    withCredentials: true,
  });

  if (!skipLoading) {
    isLoading.set(true);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      return from(extractBackendError(error)).pipe(
        switchMap((backendError) => {
          const lang = currentLang() || 'vi';
          let title = lang === 'vi' ? 'Lỗi hệ thống' : 'System error';
          let message =
            lang === 'vi' ? 'Đã xảy ra lỗi không xác định' : 'Unknown internal server error';

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
              router.navigate(['/auth/platform'], {
                queryParams: { returnUrl: router.routerState.snapshot.url },
              });
              toastrService.error(message, title);
              break;
            case 403:
              router.navigate(['/403']);
              toastrService.warning(message, title);
              break;
            case 0:
              title = lang === 'vi' ? 'Lỗi mạng' : 'Network error';
              message =
                lang === 'vi' ? 'Không thể kết nối đến máy chủ' : 'Cannot connect to server';
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
      );
    }),
    finalize(() => {
      if (!skipLoading) {
        isLoading.set(false);
      }
    }),
  );
};

async function extractBackendError(error: HttpErrorResponse): Promise<any> {
  const raw = error?.error;

  if (raw instanceof Blob && raw.type?.includes('json')) {
    try {
      const text = await raw.text();
      const json = JSON.parse(text);
      return json?.body?.data || null;
    } catch {
      return null;
    }
  }

  if (raw && typeof raw === 'object') {
    return raw?.body?.data || null;
  }

  return null;
}
