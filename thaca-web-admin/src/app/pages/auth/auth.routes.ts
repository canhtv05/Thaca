import { Routes } from '@angular/router';

export const authRoutes: Routes = [
  {
    path: '',
    redirectTo: 'verify',
    pathMatch: 'full',
  },
  {
    path: 'verify',
    loadComponent: () =>
      import('./email-verify/email-verify.component').then((m) => m.EmailVerifyComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'platform',
    loadComponent: () =>
      import('./platform-selection/platform-selection.component').then(
        (m) => m.PlatformSelectionComponent,
      ),
  },
];
