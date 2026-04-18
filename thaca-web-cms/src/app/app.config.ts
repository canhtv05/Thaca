import {
  ApplicationConfig,
  inject,
  importProvidersFrom,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AppConfigService } from './core/configs/app-config.service';
import { GlobalService } from './core/global/global.service';
import { I18nService } from './core/i18n/i18n.service';
import { StoreEffectService } from './core/services/store-effect.service';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { provideIcons } from '@ng-icons/core';
import { APP_CONFIG_ICONS } from './core/configs/app-config.icon';
import { ThemeService } from './core/theme/theme.service';
import { AuthService } from './core/services/auth.service';
import { authInterceptor } from './core/global/auth.interceptor';
import { provideToastr } from 'ngx-toastr';
import { logger } from './utils/logger';

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimations(),
    provideToastr({
      timeOut: 5000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
    }),
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
      const authService = inject(AuthService);
      const http = inject(HttpClient);
      (window as any).__appGlobal = { httpClient: http };

      await firstValueFrom(i18n.bootstrapLanguage());
      await appConfig.loadConfig();

      authService.getUserProfile().catch(() => {});
    }),
    provideIcons(
      Object.fromEntries(Object.entries(APP_CONFIG_ICONS).map(([name, icon]) => [name, icon.icon])),
    ),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
