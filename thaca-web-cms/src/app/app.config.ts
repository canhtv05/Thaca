import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AppConfigService } from './core/configs/app-config.service';
import { GlobalService } from './core/global/global.service';
import { StoreEffectService } from './core/services/store-effect.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    providePrimeNG({
      theme: {
        preset: Aura,
      },
    }),
    provideAppInitializer(async () => {
      const appConfig = inject(AppConfigService);
      const globalService = inject(GlobalService);
      const storeEffectService = inject(StoreEffectService);
      await appConfig.loadConfig();
      void globalService;
      void storeEffectService;
    }),
  ],
};
