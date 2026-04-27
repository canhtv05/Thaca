import { firstValueFrom } from 'rxjs';
import { createBody, createHeader } from '../../utils/common.utils';
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class CommonService {
  private readonly http = inject(HttpClient);

  async downloadFile(url: string, fileName: string, body?: any): Promise<void> {
    const payload = {
      header: createHeader(),
      body: createBody(body || {}),
    };

    const blob = await firstValueFrom(
      this.http.post(url, payload, { responseType: 'blob', withCredentials: true }),
    );

    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob as Blob);
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(link.href);
  }
}
