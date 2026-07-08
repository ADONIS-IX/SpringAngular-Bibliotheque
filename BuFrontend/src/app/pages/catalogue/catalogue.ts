import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LivreService } from '../../core/api.service';
import { Livre } from '../../core/models';

// Palette d'accents déterministe : chaque catégorie retombe toujours sur la même
// teinte, pour un repère visuel stable façon étagères classées par discipline.
const ACCENTS = ['accent-indigo', 'accent-gold', 'accent-teal', 'accent-plum', 'accent-slate'] as const;

@Component({
  selector: 'app-catalogue',
  imports: [
    RouterLink, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatButtonToggleModule, MatProgressSpinnerModule,
  ],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.scss',
})
export class Catalogue implements OnInit, OnDestroy {
  private livreService = inject(LivreService);
  private searchTimer?: ReturnType<typeof setTimeout>;

  livres = signal<Livre[]>([]);
  loading = signal(true);
  recherche = '';
  filtre = signal<'tous' | 'disponibles'>('tous');

  // Gabarits utilisés pour l'état de chargement (cartes fantômes)
  readonly skeletons = Array.from({ length: 8 });

  readonly livresAffiches = computed(() =>
    this.filtre() === 'disponibles'
      ? this.livres().filter((l) => l.disponible)
      : this.livres()
  );

  readonly totalDisponibles = computed(() => this.livres().filter((l) => l.disponible).length);

  ngOnInit(): void {
    this.charger();
  }

  ngOnDestroy(): void {
    clearTimeout(this.searchTimer);
  }

  onSearchInput(): void {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.charger(), 350);
  }

  charger(): void {
    this.loading.set(true);
    const source = this.recherche.trim()
      ? this.livreService.rechercher(this.recherche.trim())
      : this.livreService.lister();
    source.subscribe({
      next: (livres) => {
        this.livres.set(livres);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  auteurs(livre: Livre): string {
    return livre.auteurs.map((a) => a.nomComplet).join(', ');
  }

  // Initiale utilisée sur la vignette de couverture
  initiale(livre: Livre): string {
    return livre.titre?.trim().charAt(0).toUpperCase() || '?';
  }

  // Classe d'accent stable par catégorie (hash simple sur la chaîne)
  accent(livre: Livre): string {
    const cle = livre.categorie || 'Non classé';
    let hash = 0;
    for (let i = 0; i < cle.length; i++) {
      hash = (hash * 31 + cle.charCodeAt(i)) >>> 0;
    }
    return ACCENTS[hash % ACCENTS.length];
  }
}