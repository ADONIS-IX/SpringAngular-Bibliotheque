import { Component, inject, signal, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AuteurService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Auteur } from '../../core/models';
import { AuteurDialog } from './auteur-dialog';

@Component({
  selector: 'app-gestion-auteurs',
  imports: [
    MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatDialogModule,
  ],
  templateUrl: './gestion-auteurs.html',
  styleUrl: './gestion-auteurs.scss',
})
export class GestionAuteurs implements OnInit {
  private auteurService = inject(AuteurService);
  private dialog = inject(MatDialog);
  private ui = inject(Ui);

  auteurs = signal<Auteur[]>([]);
  loading = signal(true);
  colonnes = ['nom', 'nationalite', 'livres', 'actions'];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.auteurService.lister().subscribe({
      next: a => { this.auteurs.set(a); this.loading.set(false); },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  ouvrir(auteur?: Auteur): void {
    const ref = this.dialog.open(AuteurDialog, { data: auteur ?? null });
    ref.afterClosed().subscribe(req => {
      if (!req) return;
      const obs = auteur
        ? this.auteurService.modifier(auteur.id, req)
        : this.auteurService.creer(req);
      obs.subscribe({
        next: () => { this.ui.success('Auteur enregistré'); this.charger(); },
        error: err => this.ui.error(err),
      });
    });
  }

  supprimer(auteur: Auteur): void {
    if (!confirm(`Supprimer l'auteur ${auteur.prenom} ${auteur.nom} ?`)) return;
    this.auteurService.supprimer(auteur.id).subscribe({
      next: () => { this.ui.success('Auteur supprimé'); this.charger(); },
      error: err => this.ui.error(err),
    });
  }
}
