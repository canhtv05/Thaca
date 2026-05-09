import { Routes } from '@angular/router';
import { I18nResolver } from '../../../core/i18n/i18n.resolver';

export const systemUserRoutes: Routes = [
  {
    path: 'system-users',
    loadComponent: () => import('./system-user.component').then((m) => m.SystemUserComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['system_user'] },
  },
  {
    path: 'system-users/:targetUserId/lock-history',
    loadComponent: () =>
      import('./system-user-lock-history/system-user-lock-history.component').then(
        (m) => m.SystemUserLockHistoryComponent,
      ),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['system_user'] },
  },
  {
    path: 'system-users/:targetUserId',
    loadComponent: () =>
      import('./system-user-detail/system-user-detail.component').then(
        (m) => m.SystemUserDetailComponent,
      ),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['system_user'] },
  },
];
