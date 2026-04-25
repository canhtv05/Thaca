import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ForbiddenComponent } from './pages/403/403.component';
import { NotFoundComponent } from './pages/404/404.component';
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
    path: 'login',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['auth'] },
    canActivate: [GuestGuard],
    component: LoginComponent,
  },
  {
    path: '403',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['error'] },
    component: ForbiddenComponent,
  },
  {
    path: '404',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['error'] },
    component: NotFoundComponent,
  },
  {
    path: '**',
    redirectTo: '404',
    pathMatch: 'full',
  },
];
