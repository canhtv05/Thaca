import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { I18nResolver } from '../../core/i18n/i18n.resolver';
import { LoginHistoryComponent } from './login-history/login-history.component';

export const overviewRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    resolve: { i18n: I18nResolver },
    data: { i18n: ['user', 'auth'] },
    children: [
      {
        path: 'login-history',
        component: LoginHistoryComponent,
      },
    ],
  },
];
