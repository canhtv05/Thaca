import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IPermissionDTO } from '../permission/permission.model';
import { IMailConfigDTO } from './mail-config.model';

export interface ITestConnectionRequest {
  host: string;
  port: number;
  username: string;
  password: string;
  isAuth?: boolean;
  isStarttls?: boolean;
}

export interface ITestConnectionResponse {
  success: boolean;
  message: string;
}

export interface ITenant {
  id?: string | number;
  name?: string;
  status?: string;
}

@Injectable({
  providedIn: 'root',
})
export class MailConfigService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);
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

  async getById(id: number): Promise<IApiPayload<IMailConfigDTO>> {
    return await GlobalHttp.get<IApiPayload<IMailConfigDTO>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/${id}`,
    );
  }

  async delete(id: number): Promise<IApiPayload<any>> {
    return await GlobalHttp.delete<IApiPayload<any>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/${id}`,
    );
  }

  async testConnection(
    config: ITestConnectionRequest,
  ): Promise<IApiPayload<ITestConnectionResponse>> {
    const payload: IApiPayload<ITestConnectionRequest> = {
      header: createHeader(),
      body: createBody(config),
    };
    return await GlobalHttp.post<IApiPayload<ITestConnectionResponse>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/test`,
      payload,
    );
  }

  async getTenants(): Promise<IApiPayload<ITenant[]>> {
    return await GlobalHttp.get<IApiPayload<ITenant[]>>(
      `${this.config.getApiUrl()}/${this.baseUrl}/tenants/list`,
    );
  }
}
