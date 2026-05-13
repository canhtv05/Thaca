import { inject, Injectable, computed, signal } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, IAuthUserDTO, ILoginReq } from '../models/auth.model';
import { IApiPayload } from '../models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { currentUser } from '../stores/app.store';

type AuthState = 'unknown' | 'authenticated' | 'unauthenticated' | 'logged-out';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly config = inject(AppConfigService);

  private readonly authState = signal<AuthState>('unknown');
  private profilePromise: Promise<boolean> | null = null;

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
      `${this.config.getApiUrl()}/cms/sign-in`,
      payload,
    );
    if (res.body.status === 'OK' && res.body.data.isAuthenticate) {
      currentUser.set(res.body.data.info);
      this.authState.set('authenticated');
    }
    return res;
  }

  logout(): void {
    currentUser.set(null);
    this.profilePromise = null;
    this.authState.set('logged-out');
  }

  async logoutAsync(): Promise<void> {
    const res = await GlobalHttp.post<IApiPayload<void>>(
      `${this.config.getApiUrl()}/cms/sign-out`,
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

  isSuperAdmin(): boolean {
    return this.user()?.isSuperAdmin ?? false;
  }

  private async loadProfile(): Promise<boolean> {
    const res = await GlobalHttp.post<IApiPayload<IAuthUserDTO>>(
      `${this.config.getApiUrl()}/cms/profile`,
      { header: createHeader(), body: createBody({}) },
    );
    if (res?.body?.status === 'OK' && res.body.data) {
      currentUser.set(res.body.data);
      this.authState.set('authenticated');
      return true;
    }
    this.authState.set('unauthenticated');
    return false;
  }
}
