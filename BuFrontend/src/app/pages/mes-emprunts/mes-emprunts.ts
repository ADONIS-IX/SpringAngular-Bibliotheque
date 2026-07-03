import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmpruntService } from '../../core/api.service';
import { Ui } from '../../core/ui';
import { Emprunt } from '../../core/models';

@Component({
  selector: 'app-mes-emprunts',
  imports: [
    DatePipe, MatCardModule, MatButtonModule, MatIconModule,
    MatTabsModule, MatProgressSpinnerModule,
  ],
  templateUrl: './mes-emprunts.html',
  styleUrl: './mes-emprunts.scss',
})
export class MesEmprunts implements OnInit {
  private empruntService = inject(EmpruntService);
  private ui = inject(Ui);

  emprunts = signal<Emprunt[]>([]);
  loading = signal(true);

  enCours = computed(() => this.emprunts().filter(e => !e.dateRetourEffective));
  historique = computed(() => this.emprunts().filter(e => e.dateRetourEffective));

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.empruntService.mesEmprunts().subscribe({
      next: e => { this.emprunts.set(e); this.loading.set(false); },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  prolonger(e: Emprunt): void {
    this.empruntService.prolonger(e.id).subscribe({
      next: () => { this.ui.success('Emprunt prolongé de 7 jours'); this.charger(); },
      error: err => this.ui.error(err),
    });
  }

  classeEcheance(e: Emprunt): string {
    if (e.enRetard) return 'chip-danger';
    if (e.joursRestants <= 2) return 'chip-warn';
    return 'chip-success';
  }

  texteEcheance(e: Emprunt): string {
    if (e.enRetard) return `En retard de ${-e.joursRestants} j`;
    if (e.joursRestants === 0) return "À rendre aujourd'hui";
    return `${e.joursRestants} j restant(s)`;
  }
}
