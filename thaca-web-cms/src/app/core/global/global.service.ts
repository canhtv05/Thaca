import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class GlobalService {
  constructor() {
    (window as any).__appGlobal = {
      httpClient: inject(HttpClient),
    };
  }
}
