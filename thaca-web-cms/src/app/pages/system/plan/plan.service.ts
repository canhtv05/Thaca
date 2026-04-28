import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { IPlanDTO } from './plan.model';
import { createBody, createHeader } from '../../../utils/common.utils';
import { GlobalHttp } from '../../../core/global/global-http';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { CommonService } from '../../../core/services/common.service';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async lockUnlock(req: IPlanDTO): Promise<IApiPayload<IPlanDTO>> {
    const payload: IApiPayload<IPlanDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/cms/plans/lock-unlock`,
      payload,
    );
  }

  async update(req: IPlanDTO): Promise<IApiPayload<IPlanDTO>> {
    const payload: IApiPayload<IPlanDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/cms/plans/update`,
      payload,
    );
  }

  async create(req: IPlanDTO): Promise<IApiPayload<IPlanDTO>> {
    const payload: IApiPayload<IPlanDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/cms/plans/create`,
      payload,
    );
  }

  async exportData(req?: ISearchRequest<IPlanDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/plans/export`;
    await this.commonService.downloadFile(url, 'thaca-plans-export-{{date}}.xlsx', req || {});
  }

  async getAll(): Promise<IApiPayload<IPlanDTO[]>> {
    const payload: IApiPayload<any> = {
      header: createHeader(),
      body: createBody({}),
    };
    return await GlobalHttp.post<IApiPayload<IPlanDTO[]>>(
      `${this.config.getApiUrl()}/cms/plans/all`,
      payload,
    );
  }
}
