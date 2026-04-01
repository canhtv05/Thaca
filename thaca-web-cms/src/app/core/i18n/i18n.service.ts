import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';
import { currentLang } from '../stores/app.store';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly http = inject(HttpClient);
  private readonly translate = inject(TranslateService);

  bootstrapLanguage() {
    const lang = currentLang();
    return this.translate.use(lang);
  }

  loadRouteTranslations(route: ActivatedRouteSnapshot) {
    const files: string[] = route.data['i18n'] || [];
    const lang = currentLang();
    if (!files.length) return;
    const requests = files.map((f) => this.http.get<any>(`/assets/i18n/${lang}/${f}.json`));
    return forkJoin(requests).pipe(
      switchMap((responses) => {
        const merged = Object.assign({}, ...responses);
        this.translate.setTranslation(lang, merged, true);
        return this.translate.use(lang);
      }),
    );
  }

  switchLang() {
    let lang = currentLang();
    if (lang === 'vi') {
      lang = 'en';
    } else {
      lang = 'vi';
    }
    currentLang.set(lang);
    this.translate.use(lang);
  }
}
