import { Injectable, inject } from '@angular/core';
import { IApiPayload } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IPermissionDTO } from './permission.model';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private readonly config = inject(AppConfigService);

  async getAllPermissions(): Promise<IApiPayload<IPermissionDTO[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };

    return await GlobalHttp.post<IApiPayload<IPermissionDTO[]>>(
      `${this.config.getApiUrl()}/cms/permissions/all`,
      payload,
    );
  }
}
