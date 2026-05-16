import { Injectable, inject } from '@angular/core';
import { IApiPayload, ISearchRequest } from '../../../core/models/common.model';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { GlobalHttp } from '../../../core/global/global-http';
import { CommonService } from '../../../core/services/common.service';
import { createBody, createHeader } from '../../../utils/common.utils';
import { IPermissionDTO } from '../permission/permission.model';
import { IMailConfigDTO } from './mail-config.model';

@Injectable({
  providedIn: 'root',
})
export class MailConfigService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async create(req: IMailConfigDTO): Promise<IApiPayload<any>> {
    const payload: IApiPayload<IMailConfigDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/auth/admin/mail-configs/create`,
      payload,
    );
  }
}
