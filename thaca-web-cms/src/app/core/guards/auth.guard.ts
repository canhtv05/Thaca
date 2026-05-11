import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateFn = async (_, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const ok = await auth.ensureAuthenticated();

  return (
    ok ||
    router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    })
  );
};
