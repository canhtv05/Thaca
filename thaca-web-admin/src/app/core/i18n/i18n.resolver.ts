import { inject, Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { I18nService } from './i18n.service';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class I18nResolver implements Resolve<any> {
  private readonly i18n = inject(I18nService);

  resolve(route: ActivatedRouteSnapshot): Observable<any> | Promise<any> | any {
    return this.i18n.loadRouteTranslations(route) || of(null);
  }
}
