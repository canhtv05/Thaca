import { inject, Injectable } from '@angular/core';
import { HttpContext, HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { forkJoin, Observable, of, switchMap, tap } from 'rxjs';
import { currentLang } from '../stores/app.store';
import { SKIP_LOADING } from '../global/http-context';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly http = inject(HttpClient);
  private readonly translate = inject(TranslateService);
  private readonly baseFiles = ['validation', 'common', 'menu', 'layout'];
  private loaded = new Set<string>();
  private activeRouteFiles: string[] = [];

  bootstrapLanguage(): Observable<any> {
    const lang = localStorage.getItem('lang') || currentLang();
    if (lang !== currentLang()) {
      currentLang.set(lang);
    }
    return this.loadFiles(lang, this.baseFiles);
  }

  loadRouteTranslations(route: ActivatedRouteSnapshot) {
    const files: string[] = route.data['i18n'] || [];
    this.activeRouteFiles = files;
    const lang = currentLang();

    return this.loadFiles(lang, [...this.baseFiles, ...files]);
  }

  setLanguage(lang: 'vi' | 'en') {
    currentLang.set(lang);
    localStorage.setItem('lang', lang);
    this.loadFiles(lang, [...this.baseFiles, ...this.activeRouteFiles]).subscribe();
  }

  private loadFiles(lang: string, files: string[]) {
    const toLoad = files.filter((f) => !this.loaded.has(`${lang}-${f}`));
    if (!toLoad.length) {
      return this.translate.use(lang);
    }
    const requests = toLoad.map((file) =>
      this.http.get(`/assets/i18n/${lang}/${file}.json`, {
        context: new HttpContext().set(SKIP_LOADING, true),
      }),
    );
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
