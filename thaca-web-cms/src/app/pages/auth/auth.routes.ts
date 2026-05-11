import { Routes } from '@angular/router';

export const authRoutes: Routes = [
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
