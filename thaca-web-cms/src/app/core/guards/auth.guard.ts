import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { currentUser, isInitialAuthChecked } from '../stores/app.store';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateChildFn = async (_, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (!isInitialAuthChecked()) {
    try {
      await authService.getUserProfile();
    } catch (e) {
      isInitialAuthChecked.set(true);
    }
  }

  if (!currentUser()) {
    void router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    void authService.logout();
    return false;
  }
  return true;
};
