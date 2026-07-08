import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StatsService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Dashboard } from '../../core/models';

interface Tile {
  label: string;
  valeur: number | string;
  icon: string;
  accent: string;
  description?: string;
  trend?: 'up' | 'down' | 'neutral';
}

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class DashboardPage implements OnInit {
  private statsService = inject(StatsService);
  private ui = inject(Ui);

  data = signal<Dashboard | null>(null);
  loading = signal(true);
  refreshing = signal(false);

  // Gabarits pour l'état de chargement
  readonly skeletonTiles = Array.from({ length: 8 });

  tiles = computed<Tile[]>(() => {
    const d = this.data();
    if (!d) return [];

    const tauxDispo = d.totalExemplaires > 0 ? Math.round((d.exemplairesDisponibles / d.totalExemplaires) * 100) : 0;
    const tauxRetard = d.empruntsEnCours > 0 ? Math.round((d.empruntsEnRetard / d.empruntsEnCours) * 100) : 0;

    return [
      {
        label: 'Livres au catalogue',
        valeur: d.totalLivres,
        icon: 'library_books',
        accent: 'blue',
        description: 'Nombre total de livres référencés',
      },
      {
        label: 'Exemplaires disponibles',
        valeur: `${d.exemplairesDisponibles}/${d.totalExemplaires}`,
        icon: 'inventory_2',
        accent: 'blue',
        description: `Taux de disponibilité : ${tauxDispo}%`,
      },
      {
        label: 'Membres inscrits',
        valeur: d.totalMembres,
        icon: 'group',
        accent: 'blue',
        description: 'Utilisateurs actifs dans la bibliothèque',
      },
      {
        label: 'Emprunts en cours',
        valeur: d.empruntsEnCours,
        icon: 'import_contacts',
        accent: 'green',
        description: 'Livres actuellement empruntés',
      },
      {
        label: 'Emprunts en retard',
        valeur: d.empruntsEnRetard,
        icon: 'running_with_errors',
        accent: 'red',
        description: `Taux de retard : ${tauxRetard}%`,
        trend: d.empruntsEnRetard > 0 ? 'down' : 'neutral',
      },
      {
        label: 'Réservations en attente',
        valeur: d.reservationsEnAttente,
        icon: 'bookmark',
        accent: 'amber',
        description: 'Livres réservés par les utilisateurs',
      },
      {
        label: 'Pénalités impayées',
        valeur: d.penalitesImpayees,
        icon: 'gavel',
        accent: 'red',
        description: 'Nombre de pénalités non réglées',
      },
      {
        label: 'Montant impayé (FCFA)',
        valeur: d.montantPenalitesImpayees.toLocaleString('fr-FR'),
        icon: 'payments',
        accent: 'amber',
        description: 'Total des pénalités en attente de paiement',
      },
    ];
  });

  // Échelles des graphes
  maxMois = computed(() => Math.max(1, ...(this.data()?.empruntsParMois.map((m) => m.nombre) ?? [0])));
  maxTop = computed(() => Math.max(1, ...(this.data()?.topLivres.map((l) => l.nombreEmprunts) ?? [0])));
  totalStatuts = computed(() => (this.data()?.repartitionStatuts ?? []).reduce((s, r) => s + r.nombre, 0));

  readonly aDesDonnees = computed(() => {
    const d = this.data();
    return !!d && !!(d.empruntsParMois?.length || d.topLivres?.length || d.repartitionStatuts?.length);
  });

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.statsService.dashboard().subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: (err: any) => { this.loading.set(false); this.ui.error(err); },
    });
  }

  lancerTraitements(): void {
    this.refreshing.set(true);
    this.statsService.lancerTraitements().subscribe({
      next: (r) => {
        this.ui.success(r.resultat);
        this.refreshing.set(false);
        this.charger();
      },
      error: (err: any) => { this.refreshing.set(false); this.ui.error(err); },
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

  getTrendIcon(trend?: 'up' | 'down' | 'neutral'): string {
    switch (trend) {
      case 'up': return 'trending_up';
      case 'down': return 'trending_down';
      default: return 'trending_flat';
    }
  }

  getTrendClass(trend?: 'up' | 'down' | 'neutral'): string {
    switch (trend) {
      case 'up': return 'trend-up';
      case 'down': return 'trend-down';
      default: return 'trend-neutral';
    }
  }
}