import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { forkJoin, Observable, of, switchMap, tap } from 'rxjs';
import { currentLang } from '../stores/app.store';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly http = inject(HttpClient);
  private readonly translate = inject(TranslateService);
  private readonly baseFiles = ['validation'];
  private loaded = new Set<string>();

  bootstrapLanguage(): Observable<any> {
    const lang = currentLang();
    return this.loadFiles(lang, this.baseFiles);
  }

  loadRouteTranslations(route: ActivatedRouteSnapshot) {
    const files: string[] = route.data['i18n'] || [];
    const lang = currentLang();

    if (!files.length) return of(null);

    return this.loadFiles(lang, files);
  }

  setLanguage(lang: 'vi' | 'en') {
    currentLang.set(lang);
    void this.loadFiles(lang, this.baseFiles);
  }

  private loadFiles(lang: string, files: string[]) {
    const toLoad = files.filter((f) => !this.loaded.has(`${lang}-${f}`));
    if (!toLoad.length) {
      return of(this.translate.use(lang));
    }
    const requests = toLoad.map((file) => this.http.get(`/assets/i18n/${lang}/${file}.json`));
    return forkJoin(requests).pipe(
      tap((responses) => {
        const merged = Object.assign({}, ...responses);
        this.translate.setTranslation(lang, merged, true);
        toLoad.forEach((f) => this.loaded.add(`${lang}-${f}`));
      }),
      switchMap(() => this.translate.use(lang)),
    );
  }
}
