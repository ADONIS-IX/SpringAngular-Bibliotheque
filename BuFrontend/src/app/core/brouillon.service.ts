import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BrouillonService {
    private brouillons = new Map<string, any>();

    /** Récupère un brouillon pour une clé donnée */
    getBrouillon<T>(cle: string): T | null {
        return (this.brouillons.get(cle) as T) || null;
    }

    /** Sauvegarde un brouillon pour une clé donnée */
    sauvegarder<T>(cle: string, donnees: T): void {
        this.brouillons.set(cle, donnees);
    }

    /** Supprime un brouillon pour une clé donnée */
    effacer(cle: string): void {
        this.brouillons.delete(cle);
    }
}