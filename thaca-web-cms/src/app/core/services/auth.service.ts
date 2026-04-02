import { inject, Injectable } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IAuthenticateRes, ILoginReq } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly appConfigService = inject(AppConfigService);

  async login(req: ILoginReq) {
    return await GlobalHttp.post<IAuthenticateRes>(
      `${this.appConfigService.getApiUrl()}/auth/sign-in`,
      req,
    );
  }
}
