import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { UserListComponent } from './user-list/user-list.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const userManagementRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    resolve: { i18n: I18nResolver },
    data: { i18n: ['user'] },
    children: [
      {
        path: 'list',
        component: UserListComponent,
      },
    ],
  },
];
