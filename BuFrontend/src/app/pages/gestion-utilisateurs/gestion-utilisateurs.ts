import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Utilisateur, UpdateUserRequest } from '../../core/models';
import { UtilisateurDialog } from './utilisateur-dialog';

@Component({
  selector: 'app-gestion-utilisateurs',
  imports: [
    CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule,
    MatDialogModule, MatProgressSpinnerModule,
  ],
  templateUrl: './gestion-utilisateurs.html',
  styleUrls: ['./gestion-utilisateurs.scss'],
})
export class GestionUtilisateurs implements OnInit {
  private adminService = inject(AdminService);
  private dialog = inject(MatDialog);
  private ui = inject(Ui);

  utilisateurs = signal<Utilisateur[]>([]);
  loading = signal(true);
  colonnes = ['nom', 'email', 'role', 'actif', 'actions'];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.adminService.listerUtilisateurs().subscribe({
      next: list => {
        this.utilisateurs.set(list);
        this.loading.set(false);
      },
      error: err => {
        this.ui.error(err);
        this.loading.set(false);
      }
    });
  }

  ouvrir(): void {
    const ref = this.dialog.open(UtilisateurDialog, { data: { mode: 'create' } });
    ref.afterClosed().subscribe(req => {
      if (!req) return;
      this.adminService.creerUtilisateur(req).subscribe({
        next: () => {
          this.ui.success('Utilisateur créé');
          this.charger();
        },
        error: err => this.ui.error(err),
      });
    });
  }

  modifier(utilisateur: Utilisateur): void {
    const ref = this.dialog.open(UtilisateurDialog, { data: { mode: 'edit', utilisateur } });
    ref.afterClosed().subscribe((req: UpdateUserRequest | undefined) => {
      if (!req) return;
      this.adminService.modifierUtilisateur(utilisateur.id, req).subscribe({
        next: () => {
          this.ui.success('Utilisateur modifié');
          this.charger();
        },
        error: err => this.ui.error(err),
      });
    });
  }

  supprimer(utilisateur: Utilisateur): void {
    if (!confirm(`Supprimer l'utilisateur ${utilisateur.nom} ?`)) return;
    this.adminService.supprimerUtilisateur(utilisateur.id).subscribe({
      next: () => {
        this.ui.success('Utilisateur supprimé');
        this.charger();
      },
      error: err => this.ui.error(err),
    });
  }
}
