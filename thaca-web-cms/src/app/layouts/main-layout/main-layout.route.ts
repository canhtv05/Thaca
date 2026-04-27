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
        path: 'profile',
        loadComponent: () =>
          import('../../pages/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('../../pages/analytics/analytics.component').then((m) => m.AnalyticsComponent),
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('../../pages/notifications/notifications.component').then(
            (m) => m.NotificationsComponent,
          ),
      },
      {
        path: '',
        loadChildren: () =>
          import('../../pages/overview/overview.route').then((m) => m.overviewRoute),
      },
      {
        path: 'content',
        loadChildren: () =>
          import('../../pages/moderation/moderation.route').then((m) => m.moderationRoutes),
      },
      {
        path: 'moderation',
        loadChildren: () =>
          import('../../pages/moderation/moderation.route').then((m) => m.moderationRoutes),
      },
      {
        path: 'engagement',
        loadChildren: () =>
          import('../../pages/engagement/engagement.route').then((m) => m.engagementRoutes),
      },
      {
        path: 'system',
        loadChildren: () => import('../../pages/system/system.route').then((m) => m.systemRoutes),
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
