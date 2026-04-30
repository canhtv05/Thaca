import { Injectable, inject } from '@angular/core';
import { IApiPayload } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IRoleDTO } from './role.model';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly config = inject(AppConfigService);

  async getAllRoles(): Promise<IApiPayload<IRoleDTO[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };

    return await GlobalHttp.post<IApiPayload<IRoleDTO[]>>(
      `${this.config.getApiUrl()}/cms/roles/all`,
      payload,
    );
  }

  async getPermissionsByRoles(roleCodes: string[]): Promise<IApiPayload<any[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({ roleCodes }),
    };

    return await GlobalHttp.post<IApiPayload<any[]>>(
      `${this.config.getApiUrl()}/cms/permissions/by-roles`,
      payload,
    );
  }
}
