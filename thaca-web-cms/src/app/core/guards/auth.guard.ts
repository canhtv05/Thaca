import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';

export const AuthGuard: CanActivateChildFn = (_, state) => {
  const router = inject(Router);
  const isAuthenticated = false;
  if (!isAuthenticated) {
    void router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
  return true;
};
