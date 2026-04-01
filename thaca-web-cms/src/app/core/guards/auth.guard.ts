import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { isAuthenticated } from './guest.guard';

export const AuthGuard: CanActivateChildFn = (_, state) => {
  const router = inject(Router);
  if (!isAuthenticated) {
    void router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
  return true;
};
