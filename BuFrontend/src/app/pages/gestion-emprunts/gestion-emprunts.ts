import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmpruntService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Emprunt } from '../../core/models';

@Component({
  selector: 'app-gestion-emprunts',
  imports: [
    DatePipe, NgTemplateOutlet, MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatTabsModule, MatProgressSpinnerModule,
  ],
  templateUrl: './gestion-emprunts.html',
  styleUrl: './gestion-emprunts.scss',
})
export class GestionEmprunts implements OnInit {
  private empruntService = inject(EmpruntService);
  private ui = inject(Ui);

  tous = signal<Emprunt[]>([]);
  retards = signal<Emprunt[]>([]);
  loading = signal(true);
  colonnes = ['livre', 'emprunteur', 'dates', 'statut', 'actions'];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.empruntService.tous().subscribe({
      next: e => {
        this.tous.set(e);
        this.retards.set(e.filter(x => x.enRetard && !x.dateRetourEffective));
        this.loading.set(false);
      },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  retourner(e: Emprunt): void {
    this.empruntService.retourner(e.id).subscribe({
      next: res => {
        if (res.penalite) {
          this.ui.success(`Retour enregistré. Pénalité de ${res.penalite.montant} FCFA (${res.penalite.joursRetard} j de retard).`);
        } else {
          this.ui.success('Retour enregistré à temps. L\'exemplaire est libéré.');
        }
        this.charger();
      },
      error: err => this.ui.error(err),
    });
  }

  statutChip(e: Emprunt): string {
    if (e.dateRetourEffective) return e.statut === 'EN_RETARD' ? 'chip-warn' : 'chip-neutral';
    return e.enRetard ? 'chip-danger' : 'chip-success';
  }

  statutLabel(e: Emprunt): string {
    if (e.dateRetourEffective) return 'Rendu';
    return e.enRetard ? 'En retard' : 'En cours';
  }
}
