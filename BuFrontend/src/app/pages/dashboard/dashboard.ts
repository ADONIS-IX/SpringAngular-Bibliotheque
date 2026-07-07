import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StatsService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Dashboard } from '../../core/models';

interface Tile { label: string; valeur: number | string; icon: string; accent: string; }

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class DashboardPage implements OnInit {
  private statsService = inject(StatsService);
  private ui = inject(Ui);

  data = signal<Dashboard | null>(null);
  loading = signal(true);

  tiles = computed<Tile[]>(() => {
    const d = this.data();
    if (!d) return [];
    return [
      { label: 'Livres au catalogue', valeur: d.totalLivres, icon: 'library_books', accent: 'blue' },
      { label: 'Exemplaires (dispo/total)', valeur: `${d.exemplairesDisponibles}/${d.totalExemplaires}`, icon: 'inventory_2', accent: 'blue' },
      { label: 'Membres inscrits', valeur: d.totalMembres, icon: 'group', accent: 'blue' },
      { label: 'Emprunts en cours', valeur: d.empruntsEnCours, icon: 'import_contacts', accent: 'green' },
      { label: 'En retard', valeur: d.empruntsEnRetard, icon: 'running_with_errors', accent: 'red' },
      { label: 'Réservations en attente', valeur: d.reservationsEnAttente, icon: 'bookmark', accent: 'amber' },
      { label: 'Pénalités impayées', valeur: d.penalitesImpayees, icon: 'gavel', accent: 'red' },
      { label: 'Montant impayé (FCFA)', valeur: d.montantPenalitesImpayees, icon: 'payments', accent: 'amber' },
    ];
  });

  // Échelle du graphe mensuel
  maxMois = computed(() => Math.max(1, ...(this.data()?.empruntsParMois.map(m => m.nombre) ?? [0])));
  maxTop = computed(() => Math.max(1, ...(this.data()?.topLivres.map(l => l.nombreEmprunts) ?? [0])));
  totalStatuts = computed(() => (this.data()?.repartitionStatuts ?? []).reduce((s, r) => s + r.nombre, 0));

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.statsService.dashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  lancerTraitements(): void {
    this.statsService.lancerTraitements().subscribe({
      next: r => { this.ui.success(r.resultat); this.charger(); },
      error: err => this.ui.error(err),
    });
  }

  hauteurMois(n: number): number {
    return Math.round((n / this.maxMois()) * 100);
  }

  largeurTop(n: number): number {
    return Math.round((n / this.maxTop()) * 100);
  }

  statutClasse(statut: string): string {
    switch (statut) {
      case 'EN_COURS': return 'st-cours';
      case 'RENDU': return 'st-rendu';
      case 'EN_RETARD': return 'st-retard';
      default: return 'st-cours';
    }
  }

  statutLabel(statut: string): string {
    switch (statut) {
      case 'EN_COURS': return 'En cours';
      case 'RENDU': return 'Rendus';
      case 'EN_RETARD': return 'En retard';
      default: return statut;
    }
  }

  pourcentageStatut(n: number): number {
    const t = this.totalStatuts();
    return t === 0 ? 0 : Math.round((n / t) * 100);
  }
}
