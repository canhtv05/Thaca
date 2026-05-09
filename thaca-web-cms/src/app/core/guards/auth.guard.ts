import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateFn = async (_, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const ok = await authService.ensureAuthenticated();

  if (!ok) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }

  return true;
};
