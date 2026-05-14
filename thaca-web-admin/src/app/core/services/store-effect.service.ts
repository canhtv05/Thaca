import { effect, Injectable } from '@angular/core';
import { currentLang, pageTitle } from '../stores/app.store';

@Injectable({
  providedIn: 'root',
})
export class StoreEffectService {
  constructor() {
    this.runEffect();
  }

  private runEffect() {
    return effect(() => {
      document.title = pageTitle();
      document.documentElement.lang = currentLang();
    });
  }
}
