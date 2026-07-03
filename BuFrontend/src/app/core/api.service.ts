import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Auteur, AuteurRequest, Dashboard, Emprunt, Livre, LivreRequest,
  Notification, Penalite, Reservation,
} from './models';

@Injectable({ providedIn: 'root' })
export class LivreService {
  private http = inject(HttpClient);
  lister(): Observable<Livre[]> { return this.http.get<Livre[]>('/api/livres'); }
  disponibles(): Observable<Livre[]> { return this.http.get<Livre[]>('/api/livres/disponibles'); }
  rechercher(q: string): Observable<Livre[]> {
    return this.http.get<Livre[]>('/api/livres/recherche', { params: { q } });
  }
  detail(id: number): Observable<Livre> { return this.http.get<Livre>(`/api/livres/${id}`); }
  creer(req: LivreRequest): Observable<Livre> { return this.http.post<Livre>('/api/livres', req); }
  modifier(id: number, req: LivreRequest): Observable<Livre> { return this.http.put<Livre>(`/api/livres/${id}`, req); }
  supprimer(id: number): Observable<void> { return this.http.delete<void>(`/api/livres/${id}`); }
}

@Injectable({ providedIn: 'root' })
export class AuteurService {
  private http = inject(HttpClient);
  lister(): Observable<Auteur[]> { return this.http.get<Auteur[]>('/api/auteurs'); }
  detail(id: number): Observable<Auteur> { return this.http.get<Auteur>(`/api/auteurs/${id}`); }
  livres(id: number): Observable<Livre[]> { return this.http.get<Livre[]>(`/api/auteurs/${id}/livres`); }
  creer(req: AuteurRequest): Observable<Auteur> { return this.http.post<Auteur>('/api/auteurs', req); }
  modifier(id: number, req: AuteurRequest): Observable<Auteur> { return this.http.put<Auteur>(`/api/auteurs/${id}`, req); }
  supprimer(id: number): Observable<void> { return this.http.delete<void>(`/api/auteurs/${id}`); }
}

@Injectable({ providedIn: 'root' })
export class EmpruntService {
  private http = inject(HttpClient);
  emprunter(livreId: number): Observable<Emprunt> { return this.http.post<Emprunt>('/api/emprunts', { livreId }); }
  mesEmprunts(): Observable<Emprunt[]> { return this.http.get<Emprunt[]>('/api/emprunts/mes-emprunts'); }
  prolonger(id: number): Observable<Emprunt> { return this.http.patch<Emprunt>(`/api/emprunts/${id}/prolonger`, {}); }
  tous(): Observable<Emprunt[]> { return this.http.get<Emprunt[]>('/api/emprunts'); }
  retards(): Observable<Emprunt[]> { return this.http.get<Emprunt[]>('/api/emprunts/retards'); }
  retourner(id: number): Observable<Emprunt> { return this.http.patch<Emprunt>(`/api/emprunts/${id}/retour`, {}); }
}

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  reserver(livreId: number): Observable<Reservation> { return this.http.post<Reservation>('/api/reservations', { livreId }); }
  mesReservations(): Observable<Reservation[]> { return this.http.get<Reservation[]>('/api/reservations/mes-reservations'); }
  confirmer(id: number): Observable<Emprunt> { return this.http.post<Emprunt>(`/api/reservations/${id}/confirmer`, {}); }
  annuler(id: number): Observable<void> { return this.http.delete<void>(`/api/reservations/${id}`); }
  toutes(): Observable<Reservation[]> { return this.http.get<Reservation[]>('/api/reservations'); }
}

@Injectable({ providedIn: 'root' })
export class PenaliteService {
  private http = inject(HttpClient);
  mesPenalites(): Observable<Penalite[]> { return this.http.get<Penalite[]>('/api/penalites/mes-penalites'); }
  toutes(): Observable<Penalite[]> { return this.http.get<Penalite[]>('/api/penalites'); }
  payer(id: number): Observable<Penalite> { return this.http.patch<Penalite>(`/api/penalites/${id}/payer`, {}); }
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  mesNotifications(): Observable<Notification[]> { return this.http.get<Notification[]>('/api/notifications/mes-notifications'); }
  compteNonLues(): Observable<{ count: number }> { return this.http.get<{ count: number }>('/api/notifications/non-lues/count'); }
  marquerLue(id: number): Observable<void> { return this.http.patch<void>(`/api/notifications/${id}/lue`, {}); }
  marquerToutLu(): Observable<void> { return this.http.patch<void>('/api/notifications/tout-lu', {}); }
}

@Injectable({ providedIn: 'root' })
export class StatsService {
  private http = inject(HttpClient);
  dashboard(): Observable<Dashboard> { return this.http.get<Dashboard>('/api/stats/dashboard'); }
  lancerTraitements(): Observable<{ resultat: string }> { return this.http.post<{ resultat: string }>('/api/stats/traitements', {}); }
}
