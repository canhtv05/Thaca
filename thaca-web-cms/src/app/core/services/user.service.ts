import { inject, Injectable } from '@angular/core';
import { AppConfigService } from '../configs/app-config.service';
import { GlobalHttp } from '../global/global-http';
import { IApiPayload, ISearchRequest } from '../models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';
import { IUserDTO } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly config = inject(AppConfigService);

  async search(req: ISearchRequest<IUserDTO>): Promise<IApiPayload<ISearchRequest<IUserDTO>>> {
    const payload: IApiPayload<ISearchRequest<IUserDTO>> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<ISearchRequest<IUserDTO>>>(
      `${this.config.getApiUrl()}/admin/search`,
      payload,
    );
  }
}
