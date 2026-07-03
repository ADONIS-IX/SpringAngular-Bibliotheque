import { Component, inject, signal, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LivreService, AuteurService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Auteur, Livre } from '../../core/models';
import { LivreDialog } from './livre-dialog';

@Component({
  selector: 'app-gestion-livres',
  imports: [
    MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatDialogModule,
  ],
  templateUrl: './gestion-livres.html',
  styleUrl: './gestion-livres.scss',
})
export class GestionLivres implements OnInit {
  private livreService = inject(LivreService);
  private auteurService = inject(AuteurService);
  private dialog = inject(MatDialog);
  private ui = inject(Ui);

  livres = signal<Livre[]>([]);
  auteurs = signal<Auteur[]>([]);
  loading = signal(true);
  colonnes = ['titre', 'auteurs', 'categorie', 'stock', 'actions'];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    forkJoin({ livres: this.livreService.lister(), auteurs: this.auteurService.lister() }).subscribe({
      next: ({ livres, auteurs }) => {
        this.livres.set(livres);
        this.auteurs.set(auteurs);
        this.loading.set(false);
      },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  nomsAuteurs(l: Livre): string {
    return l.auteurs.map(a => a.nomComplet).join(', ') || '—';
  }

  ouvrir(livre?: Livre): void {
    if (this.auteurs().length === 0) {
      this.ui.error('Créez d\'abord au moins un auteur');
      return;
    }
    const ref = this.dialog.open(LivreDialog, { data: { livre: livre ?? null, auteurs: this.auteurs() } });
    ref.afterClosed().subscribe(req => {
      if (!req) return;
      const obs = livre ? this.livreService.modifier(livre.id, req) : this.livreService.creer(req);
      obs.subscribe({
        next: () => { this.ui.success('Livre enregistré'); this.charger(); },
        error: err => this.ui.error(err),
      });
    });
  }

  supprimer(livre: Livre): void {
    if (!confirm(`Supprimer « ${livre.titre} » ?`)) return;
    this.livreService.supprimer(livre.id).subscribe({
      next: () => { this.ui.success('Livre supprimé'); this.charger(); },
      error: err => this.ui.error(err),
    });
  }
}
