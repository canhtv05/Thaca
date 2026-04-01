import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const GuestGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (isAuthenticated) {
    void router.navigate(['/home']);
    return false;
  }
  return true;
};

export const isAuthenticated = false;
