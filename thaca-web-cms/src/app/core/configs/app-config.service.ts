import { inject, Injectable, signal } from '@angular/core';
import { AppConfig } from './app-config.model';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private readonly http = inject(HttpClient);
  private config = signal<AppConfig | null | { [key: string]: any }>(null);

  async loadConfig() {
    const config = await firstValueFrom(this.http.get<AppConfig>('assets/config.json'));
    this.config.set(config as AppConfig);
  }

  getApiUrl() {
    return this.config()?.apiUrl;
  }
}
