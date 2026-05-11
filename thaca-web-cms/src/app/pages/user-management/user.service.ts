import { inject, Injectable } from '@angular/core';
import { AppConfigService } from '../../core/configs/app-config.service';
import { CommonService } from '../../core/services/common.service';
import { GlobalHttp } from '../../core/global/global-http';
import { IApiPayload, IImportResult, ISearchRequest } from '../../core/models/common.model';
import { IUserDTO } from './user.model';
import { createBody, createHeader } from '../../utils/common.utils';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async getUser(req: Pick<IUserDTO, 'username'>): Promise<IApiPayload<IUserDTO>> {
    const url = `${this.config.getApiUrl()}/cms/users/detail`;
    const payload: IApiPayload<Pick<IUserDTO, 'username'>> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<IUserDTO>>(url, payload);
  }

  async downloadTemplate(): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/users/download-template`;
    await this.commonService.downloadFile(url, 'thaca-users-template-{{date}}.xlsx');
  }

  async exportUsers(request: ISearchRequest<IUserDTO>): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/users/export`;
    return this.commonService.downloadFile(url, 'thaca-users-export-{{date}}.xlsx', request);
  }

  async importUsers(file: File): Promise<IApiPayload<IImportResult>> {
    const formData = new FormData();
    formData.append('file', file);
    const url = `${this.config.getApiUrl()}/cms/users/import`;
    return await GlobalHttp.post<IApiPayload<IImportResult>>(url, formData);
  }

  async downloadFileError(importResult: IImportResult): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/users/file-error`;
    await this.commonService.downloadFile(
      url,
      'thaca-users-file-error-{{date}}.xlsx',
      importResult,
    );
  }

  async lockUnlock(req: IUserDTO): Promise<IApiPayload<any>> {
    const payload: IApiPayload<IUserDTO> = {
      header: createHeader(),
      body: createBody(req),
    };
    return await GlobalHttp.post<IApiPayload<any>>(
      `${this.config.getApiUrl()}/cms/users/lock-unlock`,
      payload,
    );
  }
}
