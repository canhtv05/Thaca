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
      import('./user-lock-history/user-lock-history.component').then((m) => m.UserLockHistory),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['system_user'] },
  },
];
