import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PenaliteService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Penalite } from '../../core/models';

@Component({
  selector: 'app-gestion-penalites',
  imports: [
    CommonModule, MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule,
  ],
  templateUrl: './gestion-penalites.html',
  styleUrls: ['./gestion-penalites.scss'],
})
export class GestionPenalites implements OnInit {
  private penaliteService = inject(PenaliteService);
  private ui = inject(Ui);

  penalites = signal<Penalite[]>([]);
  loading = signal(true);
  colonnes = ['membre', 'livre', 'joursRetard', 'montant', 'statut', 'actions'];

  totalImpaye = computed(() => this.penalites()
    .filter(p => p.statut === 'NON_PAYEE')
    .reduce((s, p) => s + p.montant, 0));

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.penaliteService.toutes().subscribe({
      next: p => {
        this.penalites.set(p);
        this.loading.set(false);
      },
      error: err => {
        this.ui.error(err);
        this.loading.set(false);
      },
    });
  }

  payer(p: Penalite): void {
    this.penaliteService.payer(p.id).subscribe({
      next: () => {
        this.ui.success('Paiement enregistré');
        this.charger();
      },
      error: err => this.ui.error(err),
    });
  }
}