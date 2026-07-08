import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PenaliteService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Penalite } from '../../core/models';

@Component({
  selector: 'app-mes-penalites',
  imports: [DatePipe, MatCardModule, MatIconModule, MatTableModule, MatProgressSpinnerModule],
  templateUrl: './mes-penalites.html',
  styleUrl: './mes-penalites.scss',
})
export class MesPenalites implements OnInit {
  private penaliteService = inject(PenaliteService);
  private ui = inject(Ui);

  penalites = signal<Penalite[]>([]);
  loading = signal(true);
  colonnes = ['livre', 'joursRetard', 'montant', 'statut', 'date'];

  // Gabarit pour l'état de chargement (lignes fantômes du tableau)
  readonly skeletons = Array.from({ length: 5 });

  readonly penalitesTriees = computed(() =>
    [...this.penalites()].sort(
      (a, b) => new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime()
    )
  );

  readonly totalImpaye = computed(() =>
    this.penalites()
      .filter((p) => p.statut === 'NON_PAYEE')
      .reduce((s, p) => s + p.montant, 0)
  );

  readonly nbImpayees = computed(
    () => this.penalites().filter((p) => p.statut === 'NON_PAYEE').length
  );

  ngOnInit(): void {
    this.penaliteService.mesPenalites().subscribe({
      next: (p) => { this.penalites.set(p); this.loading.set(false); },
      error: (err) => { this.loading.set(false); this.ui.error(err); },
    });
  }
}