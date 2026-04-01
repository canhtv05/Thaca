import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { signal } from '@angular/core';

export class GlobalHttp {
  static readonly loading = signal<boolean>(false);
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
    this.loading.set(true);
    try {
      const client = this.httpClient;
      if (client) {
        const httpHeaders = new HttpHeaders(headers);
        return await firstValueFrom(
          client.request<T>(method, url, {
            body,
            headers: httpHeaders,
          }),
        );
      }
      const fetchOptions: RequestInit = {
        method,
        headers: { 'Content-Type': 'application/json', ...headers },
        body: body ? JSON.stringify(body) : undefined,
      };

      const response = await fetch(url, fetchOptions);
      let result: any;
      try {
        result = await response.json();
      } catch {
        result = await response.text();
      }

      if (!response.ok) {
        console.error('[GlobalHttp] Fetch error:', response.status, result);
      }
      return result as T;
    } finally {
      this.loading.set(false);
    }
  }
}
