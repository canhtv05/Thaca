import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IApiPayload, ISearchRequest, ISearchResponse } from '../../../core/models/common.model';
import { IPlanDTO } from '../../../core/models/plan.model';
import { createBody, createHeader } from '../../../utils/common.utils';
import { GlobalHttp } from '../../../core/global/global-http';
import { AppConfigService } from '../../../core/configs/app-config.service';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  private readonly config = inject(AppConfigService);

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
}
