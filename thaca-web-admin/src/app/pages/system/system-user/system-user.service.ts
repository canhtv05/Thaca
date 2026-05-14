import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { ISystemUserDTO } from './system-user.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { GlobalHttp } from '../../../core/global/global-http';

@Injectable({
  providedIn: 'root',
})
export class SystemUserService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async getSystemUser(req: Pick<ISystemUserDTO, 'id'>): Promise<IApiPayload<ISystemUserDTO>> {
    const payload: IApiPayload<Pick<ISystemUserDTO, 'id'>> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<ISystemUserDTO>>(
      `${this.config.getApiUrl()}/auth/admin/system-users/get`,
      payload,
    );
  }

  async lockUnlock(req: ISystemUserDTO): Promise<IApiPayload<any>> {
    const payload: IApiPayload<ISystemUserDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/auth/admin/system-users/lock-unlock`,
      payload,
    );
  }

  async update(req: ISystemUserDTO): Promise<IApiPayload<ISystemUserDTO>> {
    const payload: IApiPayload<ISystemUserDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<ISystemUserDTO>>(
      `${this.config.getApiUrl()}/auth/admin/system-users/update`,
      payload,
    );
  }

  async create(req: ISystemUserDTO): Promise<IApiPayload<ISystemUserDTO>> {
    const payload: IApiPayload<ISystemUserDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<ISystemUserDTO>>(
      `${this.config.getApiUrl()}/auth/admin/system-users/create`,
      payload,
    );
  }

  async exportData(req?: ISearchRequest<ISystemUserDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/auth/admin/system-users/export`;
    await this.commonService.downloadFile(url, 'thaca-system-user-export-{{date}}.xlsx', req || {});
  }
}
