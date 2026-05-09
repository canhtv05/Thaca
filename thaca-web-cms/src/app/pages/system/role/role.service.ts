import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IRoleDTO } from './role.model';
import { IPermissionDTO } from '../permission/permission.model';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

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

  async getPermissionsByRoles(roleCodes: string[]): Promise<IApiPayload<IPermissionDTO[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({ roleCodes }),
    };

    return await GlobalHttp.post<IApiPayload<IPermissionDTO[]>>(
      `${this.config.getApiUrl()}/cms/permissions/by-roles`,
      payload,
    );
  }

  async exportRoles(req?: ISearchRequest<IRoleDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/roles/export`;
    await this.commonService.downloadFile(url, 'thaca-roles-export-{{date}}.xlsx', req || {});
  }
}
