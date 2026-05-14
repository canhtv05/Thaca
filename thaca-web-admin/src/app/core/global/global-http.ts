import { HttpClient, HttpHeaders, HttpContext } from '@angular/common/http';
import { firstValueFrom, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SKIP_LOADING } from '../global/http-context';
import { createBody, createHeader } from '../../utils/common.utils';
import { IApiHeader } from '../models/common.model';

export interface IRequestOptions {
  headers?: Record<string, string>;
  skipLoading?: boolean;
}

export class GlobalHttp {
  private static get httpClient(): HttpClient | null {
    return (window as any).__appGlobal?.httpClient || null;
  }

  static get<T = any>(url: string, options: IRequestOptions = {}): Promise<T> {
    return this.request<T>('GET', url, undefined, options);
  }

  static post<T = any>(url: string, body?: any, options: IRequestOptions = {}): Promise<T> {
    return this.request<T>('POST', url, body, options);
  }

  static put<T = any>(url: string, body?: any, options: IRequestOptions = {}): Promise<T> {
    return this.request<T>('PUT', url, body, options);
  }

  static delete<T = any>(url: string, options: IRequestOptions = {}): Promise<T> {
    return this.request<T>('DELETE', url, undefined, options);
  }

  static async request<T>(
    method: string,
    url: string,
    body?: any,
    options: IRequestOptions = {},
  ): Promise<T> {
    const client = this.httpClient;
    if (client) {
      const httpHeaders = new HttpHeaders(options.headers || {});
      const context = new HttpContext().set(SKIP_LOADING, !!options.skipLoading);
      let requestBody = body;
      if (body instanceof FormData) {
        const fd = body as FormData;
        fd.append('header', JSON.stringify(createHeader()));
        fd.append('body', JSON.stringify(createBody({})));
        requestBody = fd;
      }

      return await firstValueFrom(
        client
          .request<T>(method, url, {
            body: requestBody,
            headers: httpHeaders,
            context,
            withCredentials: true,
          })
          .pipe(
            catchError((error) => {
              const errorBody = error?.error;
              if (errorBody && typeof errorBody === 'object') {
                return of(errorBody as T);
              }
              return of({
                header: createHeader() as IApiHeader,
                body: {
                  status: 'FAILED',
                  data: {
                    titleVi: 'Lỗi hệ thống',
                    titleEn: 'System error',
                    messageVi: error?.message || 'Đã xảy ra lỗi không xác định',
                    messageEn: error?.message || 'Unknown error',
                  },
                },
              } as T);
            }),
          ),
      );
    }
    const headersWithApi = { ...options.headers };
    let requestBody = body;
    if (body instanceof FormData) {
      const fd = body as FormData;
      fd.append('header', JSON.stringify(createHeader()));
      fd.append('body', JSON.stringify(createBody({})));
      requestBody = fd;
      console.log(requestBody, fd);
    }

    const response = await fetch(url, {
      method,
      credentials: 'include',
      headers: headersWithApi,
      body:
        requestBody instanceof FormData
          ? requestBody
          : requestBody
            ? JSON.stringify(requestBody)
            : undefined,
    });
    const data = await this.parseResponse(response);
    if (!response.ok) {
      return data as T;
    }
    return data as T;
  }

  private static async parseResponse(response: Response) {
    const contentType = response.headers.get('content-type');
    if (contentType?.includes('application/json')) {
      return response.json();
    }
    return response.text();
  }
}
