import { Routes } from '@angular/router';
import { I18nResolver } from '../../../core/i18n/i18n.resolver';

export const tenantRoutes: Routes = [
  {
    path: 'tenants',
    loadComponent: () => import('./tenant.component').then((m) => m.TenantComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['tenant'] },
  },
  {
    path: 'tenants/:tenantCode',
    loadComponent: () =>
      import('./tenant-detail/tenant-detail.component').then((m) => m.TenantDetailComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['tenant', 'plan'] },
  },
];
