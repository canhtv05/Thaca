import { Routes } from '@angular/router';
import { SystemSettingsComponent } from './system-settings.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const systemRoutes: Routes = [
  {
    path: 'settings',
    children: [
      {
        path: 'mail',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
      {
        path: 'storage',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
      {
        path: 'api-keys',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
    ],
  },
  {
    path: 'logs',
    component: SystemSettingsComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
  {
    path: 'admins',
    component: SystemSettingsComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
];
