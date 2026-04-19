import { Route } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { UserListComponent } from './user-list/user-list.component';

export const userManagementRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    children: [
      {
        path: 'list',
        component: UserListComponent,
      },
    ],
  },
];
