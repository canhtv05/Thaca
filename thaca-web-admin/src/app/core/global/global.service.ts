import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { TranslateService } from '@ngx-translate/core';
import { PopupService } from '../services/popup.service';

@Injectable({
  providedIn: 'root',
})
export class GlobalService {
  constructor() {
    (window as any).__appGlobal = {
      httpClient: inject(HttpClient),
      toastr: inject(ToastrService),
      translate: inject(TranslateService),
      popup: inject(PopupService),
    };
  }
}
