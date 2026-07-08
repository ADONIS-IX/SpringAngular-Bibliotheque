import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReservationService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Reservation } from '../../core/models';

@Component({
  selector: 'app-mes-reservations',
  imports: [
    DatePipe, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
  ],
  templateUrl: './mes-reservations.html',
  styleUrl: './mes-reservations.scss',
})
export class MesReservations implements OnInit {
  private reservationService = inject(ReservationService);
  private router = inject(Router);
  private ui = inject(Ui);

  reservations = signal<Reservation[]>([]);
  loading = signal(true);

  // Gabarits pour l'état de chargement (lignes fantômes)
  readonly skeletons = Array.from({ length: 4 });

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.reservationService.mesReservations().subscribe({
      next: (r) => { this.reservations.set(r); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.ui.error(err); },
    });
  }

  confirmer(r: Reservation): void {
    this.reservationService.confirmer(r.id).subscribe({
      next: () => {
        this.ui.success(`« ${r.livre.titre} » emprunté avec succès !`);
        this.router.navigate(['/mes-emprunts']);
      },
      error: (err) => this.ui.error(err),
    });
  }

  annuler(r: Reservation): void {
    this.reservationService.annuler(r.id).subscribe({
      next: () => { this.ui.success('Réservation annulée'); this.charger(); },
      error: (err) => this.ui.error(err),
    });
  }

  statutChip(s: Reservation['statut']): string {
    switch (s) {
      case 'DISPONIBLE': return 'chip-success';
      case 'EN_ATTENTE': return 'chip-info';
      case 'CONFIRMEE': return 'chip-neutral';
      case 'EXPIREE': return 'chip-danger';
      case 'ANNULEE': return 'chip-neutral';
    }
  }

  statutLabel(s: Reservation['statut']): string {
    switch (s) {
      case 'DISPONIBLE': return 'Disponible à retirer';
      case 'EN_ATTENTE': return 'En file d\'attente';
      case 'CONFIRMEE': return 'Confirmée';
      case 'EXPIREE': return 'Expirée';
      case 'ANNULEE': return 'Annulée';
    }
  }

  // Accent de la vignette / liseré de ligne : reflète le statut
  accent(s: Reservation['statut']): string {
    switch (s) {
      case 'DISPONIBLE': return 'accent-success';
      case 'EN_ATTENTE': return 'accent-info';
      case 'CONFIRMEE': return 'accent-neutral';
      case 'EXPIREE': return 'accent-danger';
      case 'ANNULEE': return 'accent-muted';
    }
  }

  active(r: Reservation): boolean {
    return r.statut === 'EN_ATTENTE' || r.statut === 'DISPONIBLE';
  }

  initiale(r: Reservation): string {
    return r.livre?.titre?.trim().charAt(0).toUpperCase() || '?';
  }
}