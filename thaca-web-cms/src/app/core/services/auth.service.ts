import { inject, Injectable, computed } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, ILoginReq } from '../models/auth.model';
import { IApiPayload } from '../models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { currentUser } from '../stores/app.store';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly config = inject(AppConfigService);

  private profileRequest: Promise<any> | null = null;

  readonly user = computed(() => currentUser());
  readonly isAuthenticated = computed(() => !!currentUser());

  private get baseUrl() {
    return this.config.getApiUrl();
  }

  async login(req: ILoginReq): Promise<IAuthenticateRes> {
    const payload: IApiPayload<ILoginReq> = {
      header: createHeader(),
      body: createBody(req),
    };
    const res = await GlobalHttp.post<IAuthenticateRes>(`${this.baseUrl}/auth/sign-in`, payload);
    // await this.getUserProfile(true);
    return res;
  }

  async getUserProfile(force = false): Promise<any> {
    if (currentUser() && !force) {
      return currentUser()!;
    }
    if (this.profileRequest && !force) {
      return this.profileRequest;
    }
    this.profileRequest = GlobalHttp.get<any>(`${this.baseUrl}/users/me`)
      .then((profile) => {
        currentUser.set(profile);
        return profile;
      })
      .finally(() => {
        this.profileRequest = null;
      });
    return this.profileRequest;
  }

  logout() {
    currentUser.set(null);
    this.profileRequest = null;
  }
}
