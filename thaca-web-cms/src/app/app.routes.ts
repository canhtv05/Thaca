import { Routes } from '@angular/router';
import { I18nResolver } from './core/i18n/i18n.resolver';
import { GuestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full',
  },
  {
    path: '',
    loadChildren: () =>
      import('./layouts/main-layout/main-layout.route').then((m) => m.mainLayoutRoute),
  },
  {
    path: 'auth',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['auth'] },
    canActivate: [GuestGuard],
    loadChildren: () => import('./pages/auth/auth.routes').then((m) => m.authRoutes),
  },
  {
    path: '403',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['error'] },
    loadComponent: () => import('./pages/403/403.component').then((m) => m.ForbiddenComponent),
  },
  {
    path: '404',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['error'] },
    loadComponent: () => import('./pages/404/404.component').then((m) => m.NotFoundComponent),
  },
  {
    path: '**',
    redirectTo: '404',
    pathMatch: 'full',
  },
];
