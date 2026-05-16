import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IMailConfigDTO, ITestConnectionReq, ITestConnectionRes } from './mail-config.model';

@Injectable({
  providedIn: 'root',
})
export class MailConfigService {
  private readonly config = inject(AppConfigService);
  private readonly baseUrl = 'notification/mail-config';

  async search(req: ISearchRequest<IMailConfigDTO>): Promise<IApiPayload<any>> {
    const payload: IApiPayload<ISearchRequest<IMailConfigDTO>> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/search`,
      payload,
    );
  }

  async create(req: IMailConfigDTO): Promise<IApiPayload<any>> {
    const payload: IApiPayload<IMailConfigDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/create`,
      payload,
    );
  }

  async update(req: IMailConfigDTO): Promise<IApiPayload<any>> {
    const payload: IApiPayload<IMailConfigDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/update`,
      payload,
    );
  }

  async testConnection(config: ITestConnectionReq): Promise<IApiPayload<ITestConnectionRes>> {
    const payload: IApiPayload<ITestConnectionReq> = {
      header: createHeader(),
      body: createBody(config),
    };
    return await GlobalHttp.post<IApiPayload<ITestConnectionRes>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/test`,
      payload,
      { skipLoading: true },
    );
  }
}
