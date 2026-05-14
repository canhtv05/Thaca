import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GlobalHttp } from '../../core/global/global-http';
import { AppConfigService } from '../../core/configs/app-config.service';
import { IApiPayload } from '../../core/models/common.model';
import { firstValueFrom } from 'rxjs';
import { createBody, createHeader } from '../../utils/common.utils';

@Injectable({
  providedIn: 'root',
})
export class ExcelService {
  private readonly config = inject(AppConfigService);
  private readonly http = inject(HttpClient);

  /**
   * Import file sử dụng MultipartFile (FormData) qua GlobalHttp POST
   */
  async importFile(file: File): Promise<IApiPayload<any>> {
    const formData = new FormData();
    formData.append('file', file);

    const url = `${this.config.getApiUrl()}/admin/example/excel/import`;
    return await GlobalHttp.post<IApiPayload<any>>(url, formData);
  }

  /**
   * Tải template qua POST
   */
  async downloadTemplate(): Promise<void> {
    const url = `${this.config.getApiUrl()}/admin/example/excel/template`;
    await this.postForBlob(url, 'thaca-employees-template.xlsx');
  }

  /**
   * Xuất dữ liệu qua POST
   */
  async exportData(): Promise<void> {
    const url = `${this.config.getApiUrl()}/admin/example/excel/export`;
    await this.postForBlob(url, 'thaca-employees-export.xlsx');
  }

  /**
   * Helper xử lý POST để nhận file Blob.
   * Tuân thủ ApiPayload pattern cho header/body.
   */
  private async postForBlob(url: string, fileName: string): Promise<void> {
    const payload = {
      header: createHeader(),
      body: createBody({}),
    };

    const blob = await firstValueFrom(
      this.http.post(url, payload, { responseType: 'blob', withCredentials: true }),
    );

    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(link.href);
  }
}
