import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Utilisateur } from '../../core/models';
import { UtilisateurDialog } from './utilisateur-dialog';

@Component({
  selector: 'app-gestion-utilisateurs',
  imports: [
    CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
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
  colonnes = ['nom', 'email', 'role'];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    // TODO: ajouter un endpoint de listing utilisateurs si nécessaire
    this.loading.set(false);
  }

  ouvrir(): void {
    const ref = this.dialog.open(UtilisateurDialog);
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
}
