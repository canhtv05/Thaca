import { Route } from '@angular/router';
import { MainLayoutComponent } from './main-layout.component';
import { HomeComponent } from '../../pages/home/home.component';
import { AuthGuard } from '../../core/guards/auth.guard';

export const mainLayoutRoute: Route[] = [
  {
    path: '',
    canActivateChild: [AuthGuard],
    component: MainLayoutComponent,
    children: [
      {
        path: 'home',
        component: HomeComponent,
      },
      {
        path: '',
        loadChildren: () =>
          import('../../pages/overview/overview.route').then((m) => m.overviewRoute),
      },
      {
        path: 'user-management',
        loadChildren: () =>
          import('../../pages/user-management/user-management.route').then(
            (m) => m.userManagementRoute,
          ),
      },
    ],
  },
];
