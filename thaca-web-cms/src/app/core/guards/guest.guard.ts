import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { currentUser, isInitialAuthChecked } from '../stores/app.store';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';

export const GuestGuard: CanActivateFn = () => {
  const router = inject(Router);

  return toObservable(isInitialAuthChecked).pipe(
    filter((checked) => checked),
    take(1),
    map(() => {
      if (currentUser()) {
        void router.navigate(['/home']);
        return false;
      }
      return true;
    }),
  );
};
