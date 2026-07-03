import { Component, inject, signal, OnInit } from '@angular/core';
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

@Component({
  selector: 'app-catalogue',
  imports: [
    RouterLink, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatButtonToggleModule, MatProgressSpinnerModule,
  ],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.scss',
})
export class Catalogue implements OnInit {
  private livreService = inject(LivreService);

  livres = signal<Livre[]>([]);
  loading = signal(true);
  recherche = '';
  filtre = signal<'tous' | 'disponibles'>('tous');

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    const source = this.recherche.trim()
      ? this.livreService.rechercher(this.recherche.trim())
      : this.livreService.lister();
    source.subscribe({
      next: livres => { this.livres.set(livres); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  livresAffiches(): Livre[] {
    return this.filtre() === 'disponibles'
      ? this.livres().filter(l => l.disponible)
      : this.livres();
  }

  auteurs(livre: Livre): string {
    return livre.auteurs.map(a => a.nomComplet).join(', ');
  }
}
