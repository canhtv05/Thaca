import { inject, Injectable, computed } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, IAuthUserDTO, ILoginReq } from '../models/auth.model';
import { IApiPayload } from '../models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { currentUser } from '../stores/app.store';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly config = inject(AppConfigService);
  private profilePromise: Promise<boolean> | null = null;
  private profileChecked = false;

  readonly user = computed(() => currentUser());
  readonly isAuthenticated = computed(() => !!currentUser());

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
      this.profileChecked = true;
    }
    return res;
  }

  ensureAuthenticated(): Promise<boolean> {
    if (this.isAuthenticated()) {
      return Promise.resolve(true);
    }

    if (this.profileChecked) {
      return Promise.resolve(false);
    }

    if (!this.profilePromise) {
      this.profilePromise = this.loadProfile().finally(() => {
        this.profilePromise = null;
        this.profileChecked = true;
      });
    }

    return this.profilePromise;
  }

  private async loadProfile(): Promise<boolean> {
    const profile = await GlobalHttp.post<IApiPayload<IAuthUserDTO>>(
      `${this.config.getApiUrl()}/cms/profile`,
      {
        header: createHeader(),
        body: createBody({}),
      },
    );
    if (profile?.body?.status === 'OK' && profile?.body?.data) {
      currentUser.set(profile?.body?.data);
      return true;
    }
    return false;
  }

  async getUserProfile(): Promise<IApiPayload<IAuthUserDTO>> {
    if (this.isAuthenticated()) {
      return { header: null, body: { status: 'OK', data: currentUser() } } as any;
    }

    const ok = await this.loadProfile();
    return { header: null, body: { status: ok ? 'OK' : 'FAILED', data: currentUser() } } as any;
  }

  async generateCaptcha(): Promise<IApiPayload<{ image: string; captchaId: string }>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };
    return await GlobalHttp.post<IApiPayload<{ image: string; captchaId: string }>>(
      `${this.config.getApiUrl()}/auth/generate-captcha`,
      payload,
    );
  }

  logout() {
    currentUser.set(null);
    this.profilePromise = null;
    this.profileChecked = true;
  }

  async logoutAsync(): Promise<void> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };
    const res = await GlobalHttp.post<IApiPayload<void>>(
      `${this.config.getApiUrl()}/cms/sign-out`,
      payload,
    );
    if (res.body.status === 'OK') {
      this.logout();
    }
  }
}
