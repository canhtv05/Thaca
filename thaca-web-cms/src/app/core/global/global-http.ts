import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export class GlobalHttp {
  private static get httpClient(): HttpClient | null {
    return (window as any).__appGlobal?.httpClient || null;
  }

  static get<T = any>(url: string, headers: Record<string, string> = {}): Promise<T> {
    return this.request<T>('GET', url, undefined, headers);
  }

  static post<T = any>(url: string, body?: any, headers: Record<string, string> = {}): Promise<T> {
    return this.request<T>('POST', url, body, headers);
  }

  static put<T = any>(url: string, body?: any, headers: Record<string, string> = {}): Promise<T> {
    return this.request<T>('PUT', url, body, headers);
  }

  static delete<T = any>(url: string, headers: Record<string, string> = {}): Promise<T> {
    return this.request<T>('DELETE', url, undefined, headers);
  }

  static async request<T>(
    method: string,
    url: string,
    body?: any,
    headers: Record<string, string> = {},
  ): Promise<T> {
    const client = this.httpClient;
    if (client) {
      const httpHeaders = new HttpHeaders(headers);
      return await firstValueFrom(
        client.request<T>(method, url, {
          body,
          headers: httpHeaders,
          withCredentials: true,
        }),
      );
    }
    const response = await fetch(url, {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
      body: body ? JSON.stringify(body) : undefined,
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
