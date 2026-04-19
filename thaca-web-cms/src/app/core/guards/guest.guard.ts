import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { currentUser } from '../stores/app.store';

export const GuestGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (currentUser()) {
    void router.navigate(['/home']);
    return false;
  }
  return true;
};
