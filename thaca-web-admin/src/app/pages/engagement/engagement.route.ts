import { Routes } from '@angular/router';
import { PointSystemComponent } from './point-system.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const engagementRoutes: Routes = [
  {
    path: 'points',
    children: [
      {
        path: 'rules',
        component: PointSystemComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
      {
        path: 'history',
        component: PointSystemComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
    ],
  },
];
