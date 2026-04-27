import { HttpClient, HttpHeaders, HttpContext } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { SKIP_LOADING } from '../global/http-context';

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

      return await firstValueFrom(
        client.request<T>(method, url, {
          body,
          headers: httpHeaders,
          context,
          withCredentials: true,
        }),
      );
    }
    const isFormData = body instanceof FormData;
    const response = await fetch(url, {
      method,
      credentials: 'include',
      headers: {
        ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
        ...options.headers,
      },
      body: isFormData ? body : body ? JSON.stringify(body) : undefined,
    });
    const data = await this.parseResponse(response);
    if (!response.ok) {
      throw {
        status: response.status,
        message: data?.message || 'Request failed',
        data,
      };
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
