import { inject, Injectable, signal, computed } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, ILoginReq } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly config = inject(AppConfigService);

  private readonly _user = signal<any | null>(null);
  private profileRequest: Promise<any> | null = null;

  readonly user = computed(() => this._user());
  readonly isAuthenticated = computed(() => !!this._user());

  private get baseUrl() {
    return this.config.getApiUrl();
  }

  async login(req: ILoginReq): Promise<IAuthenticateRes> {
    const res = await GlobalHttp.post<IAuthenticateRes>(`${this.baseUrl}/auth/sign-in`, req);
    // await this.getUserProfile(true);
    return res;
  }

  async getUserProfile(force = false): Promise<any> {
    if (this._user() && !force) {
      return this._user()!;
    }
    if (this.profileRequest && !force) {
      return this.profileRequest;
    }
    this.profileRequest = GlobalHttp.get<any>(`${this.baseUrl}/users/me`)
      .then((profile) => {
        this._user.set(profile);
        return profile;
      })
      .finally(() => {
        this.profileRequest = null;
      });
    return this.profileRequest;
  }

  logout() {
    this._user.set(null);
    this.profileRequest = null;
  }
}
