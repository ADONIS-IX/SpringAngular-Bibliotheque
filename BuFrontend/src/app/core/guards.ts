import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Exige une session active, sinon redirige vers /login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};

/** Réservé aux bibliothécaires / admins. */
export const bibliothecaireGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isBibliothecaire()) {
    return true;
  }
  router.navigate(['/catalogue']);
  return false;
};

/** Réservé aux administrateurs uniquement. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAdmin()) {
    return true;
  }
  router.navigate(['/catalogue']);
  return false;
};
