import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IPermissionDTO } from './permission.model';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async getAllPermissions(): Promise<IApiPayload<IPermissionDTO[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };

    return await GlobalHttp.post<IApiPayload<IPermissionDTO[]>>(
      `${this.config.getApiUrl()}/auth/admin/permissions/all`,
      payload,
    );
  }

  async exportPermissions(req?: ISearchRequest<IPermissionDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/auth/admin/permissions/export`;
    await this.commonService.downloadFile(url, 'thaca-permissions-export-{{date}}.xlsx', req || {});
  }
}
