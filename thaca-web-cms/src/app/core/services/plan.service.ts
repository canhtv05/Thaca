import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ISearchRequest, ISearchResponse } from '../models/common.model';
import { PlanDTO } from '../models/plan.model';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  private http = inject(HttpClient);
  private baseUrl = '/cms/plans';

  search(request: ISearchRequest<PlanDTO>): Observable<ISearchResponse<PlanDTO>> {
    return this.http.post<ISearchResponse<PlanDTO>>(`${this.baseUrl}/search`, request);
  }

  getById(id: number): Observable<PlanDTO> {
    return this.http.post<PlanDTO>(`${this.baseUrl}/get`, id);
  }

  save(dto: PlanDTO): Observable<PlanDTO> {
    return this.http.post<PlanDTO>(`${this.baseUrl}/save`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/delete`, id);
  }
}
