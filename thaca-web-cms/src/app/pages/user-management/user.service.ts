import { inject, Injectable } from '@angular/core';
import { AppConfigService } from '../../core/configs/app-config.service';
import { CommonService } from '../../core/services/common.service';
import { GlobalHttp } from '../../core/global/global-http';
import { IApiPayload, IImportResult } from '../../core/models/common.model';
import { createBody, createHeader } from '../../utils/common.utils';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly config = inject(AppConfigService);
  private readonly commonService = inject(CommonService);

  async downloadTemplate(): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/users/download-template`;
    await this.commonService.downloadFile(url, 'thaca-users-template-{{date}}.xlsx');
  }

  async importUsers(file: File): Promise<IApiPayload<IImportResult>> {
    const formData = new FormData();
    formData.append('file', file);
    const url = `${this.config.getApiUrl()}/cms/users/import`;
    return await GlobalHttp.post<IApiPayload<IImportResult>>(url, formData);
  }

  async downloadExportError(importResult: IImportResult): Promise<void> {
    const url = `${this.config.getApiUrl()}/cms/users/export-error`;
    await this.commonService.downloadFile(
      url,
      'thaca-users-export-error-{{date}}.xlsx',
      importResult,
    );
  }
}
