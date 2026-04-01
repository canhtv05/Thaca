import {
  ApplicationConfig,
  inject,
  importProvidersFrom,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AppConfigService } from './core/configs/app-config.service';
import { GlobalService } from './core/global/global.service';
import { I18nService } from './core/i18n/i18n.service';
import { StoreEffectService } from './core/services/store-effect.service';
import { firstValueFrom } from 'rxjs';
import { provideIcons } from '@ng-icons/core';
import { APP_CONFIG_ICONS } from './core/configs/app-config.icon';
import { ThemeService } from './core/theme/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    importProvidersFrom(TranslateModule.forRoot()),
    providePrimeNG({
      theme: {
        preset: Aura,
      },
    }),
    provideAppInitializer(async () => {
      inject(ThemeService);
      const i18n = inject(I18nService);
      const appConfig = inject(AppConfigService);
      const globalService = inject(GlobalService);
      const storeEffectService = inject(StoreEffectService);
      await firstValueFrom(i18n.bootstrapLanguage());
      await appConfig.loadConfig();
      void globalService;
      void storeEffectService;
    }),
    provideIcons(
      Object.fromEntries(
        Object.entries(APP_CONFIG_ICONS).map(([name, config]) => [name, config.icon]),
      ),
    ),
  ],
};
