import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../pages/auth/auth.service';

export const GuestGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const ok = await auth.ensureAuthenticated();
  return ok ? router.parseUrl('/home') : true;
};
