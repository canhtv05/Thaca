import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const userManagementRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    resolve: { i18n: I18nResolver },
    data: { i18n: ['user', 'auth', 'menu', 'tenant', 'system_user'] },
    children: [
      {
        path: 'users',
        loadComponent: () =>
          import('./user-list/user-list.component').then((m) => m.UserListComponent),
      },
      {
        path: 'users/:username',
        loadComponent: () =>
          import('./user-detail/user-detail.component').then((m) => m.UserDetailComponent),
      },
      {
        path: 'users/:targetUserId/login-history',
        loadComponent: () =>
          import('../overview/login-history/login-history.component').then(
            (m) => m.LoginHistoryComponent,
          ),
        resolve: { i18n: I18nResolver },
        data: {
          i18n: ['user', 'auth'],
          viewMode: 'user',
        },
      },
      {
        path: 'users/:targetUserId/lock-history',
        loadComponent: () =>
          import('../overview/lock-history/lock-history.component').then(
            (m) => m.LockHistoryComponent,
          ),
        data: {
          viewMode: 'user',
        },
      },
      {
        path: 'verify',
        loadComponent: () =>
          import('./user-verification.component').then((m) => m.UserVerificationComponent),
      },
    ],
  },
];
