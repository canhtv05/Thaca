import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { ITenantDTO, ITenantInfoPrj } from './tenant.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { GlobalHttp } from '../../../core/global/global-http';

@Injectable({
  providedIn: 'root',
})
export class TenantService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async getAll(): Promise<IApiPayload<ITenantInfoPrj[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };
    return await GlobalHttp.post<IApiPayload<ITenantInfoPrj[]>>(
      `${this.config.getApiUrl()}/admin/tenants/all`,
      payload,
    );
  }

  async getTenant(req: Pick<ITenantDTO, 'code'>): Promise<IApiPayload<ITenantDTO>> {
    const payload: IApiPayload<Pick<ITenantDTO, 'code'>> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<ITenantDTO>>(
      `${this.config.getApiUrl()}/admin/tenants/get`,
      payload,
    );
  }

  async lockUnlock(req: ITenantDTO): Promise<IApiPayload<ITenantDTO>> {
    const payload: IApiPayload<ITenantDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/admin/tenants/lock-unlock`,
      payload,
    );
  }

  async update(req: ITenantDTO): Promise<IApiPayload<ITenantDTO>> {
    const payload: IApiPayload<ITenantDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/admin/tenants/update`,
      payload,
    );
  }

  async create(req: ITenantDTO): Promise<IApiPayload<ITenantDTO>> {
    const payload: IApiPayload<ITenantDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/admin/tenants/create`,
      payload,
    );
  }

  async exportData(req?: ISearchRequest<ITenantDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/admin/tenants/export`;
    await this.commonService.downloadFile(url, 'thaca-tenants-export-{{date}}.xlsx', req || {});
  }
}
