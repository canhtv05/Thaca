import { inject, Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AppConfigService } from '../../core/configs/app-config.service';
import { GlobalHttp } from '../../core/global/global-http';
import { IAuthenticateRes, IAuthUserDTO, ILoginReq } from '../../core/models/auth.model';
import { IApiPayload } from '../../core/models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { currentUser } from '../../core/stores/app.store';

type AuthState = 'unknown' | 'authenticated' | 'unauthenticated' | 'logged-out';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly config = inject(AppConfigService);
  private readonly router = inject(Router);

  private readonly authState = signal<AuthState>('unknown');
  private profilePromise: Promise<boolean> | null = null;
  readonly verifiedEmail = signal<string | null>(null);

  readonly user = computed(() => currentUser());
  readonly isAuthenticated = computed(() => this.authState() === 'authenticated');

  ensureAuthenticated(): Promise<boolean> {
    switch (this.authState()) {
      case 'authenticated':
        return Promise.resolve(true);
      case 'unauthenticated':
      case 'logged-out':
        return Promise.resolve(false);
      case 'unknown':
      default:
        if (window.location.pathname.includes('/auth/')) {
          return Promise.resolve(false);
        }
        if (!this.hasUsableAccessToken()) {
          this.clearAuthState('unauthenticated');
          return Promise.resolve(false);
        }
        if (!this.profilePromise) {
          this.profilePromise = this.loadProfile().finally(() => {
            this.profilePromise = null;
          });
        }
        return this.profilePromise;
    }
  }

  async login(req: ILoginReq): Promise<IApiPayload<IAuthenticateRes>> {
    const payload: IApiPayload<ILoginReq> = {
      header: createHeader(),
      body: createBody(req),
    };
    const res = await GlobalHttp.post<IApiPayload<IAuthenticateRes>>(
      `${this.config.getApiUrl()}/auth/admin/sign-in`,
      payload,
    );
    if (res.body.status === 'OK' && res.body.data.isAuthenticate) {
      localStorage.setItem('thaca-access-token', res.body.data.accessToken);
      currentUser.set(res.body.data.info);
      this.authState.set('authenticated');
    }
    return res;
  }

  logout(): void {
    this.clearAuthState('logged-out');
    this.router.navigate(['/auth/verify']);
  }

  async logoutAsync(): Promise<void> {
    const res = await GlobalHttp.post<IApiPayload<void>>(
      `${this.config.getApiUrl()}/auth/admin/sign-out`,
      { header: createHeader(), body: createBody({}) },
    );
    if (res.body.status === 'OK') {
      this.logout();
    }
  }

  async generateCaptcha(): Promise<IApiPayload<{ image: string; captchaId: string }>> {
    return GlobalHttp.post<IApiPayload<{ image: string; captchaId: string }>>(
      `${this.config.getApiUrl()}/auth/generate-captcha`,
      { header: createHeader(), body: createBody({}) },
    );
  }

  async sendAuthenticateOtp(email: string): Promise<IApiPayload<void>> {
    return GlobalHttp.post<IApiPayload<void>>(
      `${this.config.getApiUrl()}/auth/admin/send-authenticate-otp`,
      { header: createHeader(), body: createBody({ email }) },
    );
  }

  isSuperAdmin(): boolean {
    return this.user()?.isSuperAdmin ?? false;
  }

  private async loadProfile(): Promise<boolean> {
    const res = await GlobalHttp.post<IApiPayload<IAuthUserDTO>>(
      `${this.config.getApiUrl()}/auth/admin/profile`,
      { header: createHeader(), body: createBody({}) },
    );
    if (res?.body?.status === 'OK' && res.body.data) {
      currentUser.set(res.body.data);
      this.authState.set('authenticated');
      return true;
    }
    this.clearAuthState('unauthenticated');
    return false;
  }

  private hasUsableAccessToken(): boolean {
    const token = localStorage.getItem('thaca-access-token');
    if (!token) {
      return false;
    }

    const exp = this.readTokenExpiry(token);
    if (!exp) {
      return true;
    }

    const nowInSeconds = Math.floor(Date.now() / 1000);
    return exp > nowInSeconds;
  }

  private readTokenExpiry(token: string): number | null {
    try {
      const [, payload] = token.split('.');
      if (!payload) {
        return null;
      }
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(
        normalized.length + ((4 - (normalized.length % 4)) % 4),
        '=',
      );
      const decoded = JSON.parse(atob(padded)) as { exp?: unknown };
      return typeof decoded.exp === 'number' ? decoded.exp : null;
    } catch {
      return null;
    }
  }

  private clearAuthState(state: AuthState): void {
    localStorage.removeItem('thaca-access-token');
    currentUser.set(null);
    this.profilePromise = null;
    this.authState.set(state);
  }
}
