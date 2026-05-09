import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const GuestGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const authenticated = await authService.ensureAuthenticated();

  if (authenticated) {
    return router.parseUrl('/home');
  }

  return true;
};
