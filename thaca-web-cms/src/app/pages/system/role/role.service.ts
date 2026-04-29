import { Injectable, inject } from '@angular/core';
import { IApiPayload } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly config = inject(AppConfigService);

  async searchRoles(req: any): Promise<IApiPayload<any>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/cms/roles/search`,
      payload,
    );
  }
}
