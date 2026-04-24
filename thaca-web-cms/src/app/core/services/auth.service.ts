import { inject, Injectable, computed } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, IAuthUserDTO, ILoginReq } from '../models/auth.model';
import { IApiPayload } from '../models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { currentUser, isInitialAuthChecked } from '../stores/app.store';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly config = inject(AppConfigService);

  private profileRequest: Promise<any> | null = null;

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
    if (res.body.status === 'OK' && res.body.data.authenticate) {
      currentUser.set(res.body.data.info);
    }
    return res;
  }

  async getUserProfile(force = false): Promise<IApiPayload<IAuthUserDTO>> {
    if (currentUser() && !force) {
      return { body: currentUser() } as any;
    }
    if (isInitialAuthChecked() && !force) {
      return { body: { status: 'FAILED' } } as any;
    }
    if (this.profileRequest && !force) {
      return this.profileRequest;
    }
    this.profileRequest = GlobalHttp.post<IApiPayload<IAuthUserDTO>>(
      `${this.config.getApiUrl()}/cms/profile`,
      {
        header: createHeader(),
        body: createBody({}),
      },
    )
      .then((profile) => {
        currentUser.set(profile.body);
        return profile;
      })
      .finally(() => {
        this.profileRequest = null;
        isInitialAuthChecked.set(true);
      });
    return this.profileRequest;
  }

  logout() {
    currentUser.set(null);
    isInitialAuthChecked.set(false);
    this.profileRequest = null;
  }
}
