import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const overviewRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    resolve: { i18n: I18nResolver },
    data: { i18n: ['user', 'auth', 'system_user'] },
    children: [
      {
        path: 'login-history',
        loadComponent: () =>
          import('./login-history/login-history.component').then((m) => m.LoginHistoryComponent),
      },
      {
        path: ':targetUserId/lock-history',
        data: { viewMode: 'current-user' },
        loadComponent: () =>
          import('./lock-history/lock-history.component').then((m) => m.LockHistoryComponent),
      },
    ],
  },
];
