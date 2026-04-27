import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ISearchRequest, ISearchResponse } from '../../../core/models/common.model';
import { TenantDTO } from '../../../core/models/tenant.model';

@Injectable({
  providedIn: 'root',
})
export class TenantService {
  private http = inject(HttpClient);
  private baseUrl = '/cms/tenants';

  search(request: ISearchRequest<TenantDTO>): Observable<ISearchResponse<TenantDTO>> {
    return this.http.post<ISearchResponse<TenantDTO>>(`${this.baseUrl}/search`, request);
  }

  getById(id: number): Observable<TenantDTO> {
    return this.http.post<TenantDTO>(`${this.baseUrl}/get`, id);
  }

  save(dto: TenantDTO): Observable<TenantDTO> {
    return this.http.post<TenantDTO>(`${this.baseUrl}/save`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/delete`, id);
  }
}
