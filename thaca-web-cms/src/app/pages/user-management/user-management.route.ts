import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { UserListComponent } from './user-list/user-list.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';
import { LoginHistoryComponent } from '../overview/login-history/login-history.component';

export const userManagementRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    resolve: { i18n: I18nResolver },
    data: { i18n: ['user', 'auth', 'menu'] },
    children: [
      {
        path: 'list',
        component: UserListComponent,
      },
      {
        path: 'verify',
        loadComponent: () =>
          import('./user-verification.component').then((m) => m.UserVerificationComponent),
      },
      {
        path: 'permissions',
        component: UserListComponent, // Reuse or change to dedicated component later
      },
      {
        path: 'login-history',
        component: LoginHistoryComponent,
      },
    ],
  },
];
