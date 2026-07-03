import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

/** Petites aides UI : messages de succès/erreur uniformes. */
@Injectable({ providedIn: 'root' })
export class Ui {
  private snack = inject(MatSnackBar);

  success(message: string): void {
    this.snack.open(message, 'OK', { duration: 3500, panelClass: 'snack-success' });
  }

  error(err: unknown): void {
    this.snack.open(this.extract(err), 'Fermer', { duration: 5000, panelClass: 'snack-error' });
  }

  /** Récupère le message métier renvoyé par l'API (ErrorResponse.message). */
  private extract(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.error && typeof err.error === 'object' && 'message' in err.error) {
        return (err.error as { message: string }).message;
      }
      if (err.status === 0) return 'Serveur injoignable. Le backend est-il démarré ?';
      return `Erreur ${err.status}`;
    }
    return 'Une erreur est survenue';
  }
}
