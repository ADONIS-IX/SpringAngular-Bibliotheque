import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { LivreService, EmpruntService, ReservationService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Ui } from '../../core/ui';
import { Livre } from '../../core/models';

// Même palette d'accents que le catalogue, pour une identité visuelle continue
// entre la carte de la grille et sa fiche détaillée.
const ACCENTS = ['accent-indigo', 'accent-gold', 'accent-teal', 'accent-plum', 'accent-slate'] as const;

@Component({
  selector: 'app-livre-detail',
  imports: [
    RouterLink, MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatProgressSpinnerModule, MatDividerModule,
  ],
  templateUrl: './livre-detail.html',
  styleUrl: './livre-detail.scss',
})
export class LivreDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private livreService = inject(LivreService);
  private empruntService = inject(EmpruntService);
  private reservationService = inject(ReservationService);
  private auth = inject(AuthService);
  private ui = inject(Ui);

  livre = signal<Livre | null>(null);
  loading = signal(true);
  action = signal(false);
  readonly isLoggedIn = this.auth.isLoggedIn;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.livreService.detail(id).subscribe({
      next: l => { this.livre.set(l); this.loading.set(false); },
      error: err => { this.loading.set(false); this.ui.error(err); },
    });
  }

  emprunter(): void {
    if (!this.exigeConnexion()) return;
    const l = this.livre();
    if (!l) return;
    this.action.set(true);
    this.empruntService.emprunter(l.id).subscribe({
      next: () => {
        this.ui.success(`« ${l.titre} » emprunté. À rendre sous 14 jours.`);
        this.rafraichir(l.id);
      },
      error: err => { this.action.set(false); this.ui.error(err); },
    });
  }

  reserver(): void {
    if (!this.exigeConnexion()) return;
    const l = this.livre();
    if (!l) return;
    this.action.set(true);
    this.reservationService.reserver(l.id).subscribe({
      next: r => {
        this.ui.success(`Réservation enregistrée — vous êtes en position ${r.position} dans la file.`);
        this.rafraichir(l.id);
      },
      error: err => { this.action.set(false); this.ui.error(err); },
    });
  }

  auteurs(livre: Livre): string {
    return livre.auteurs.map((a) => a.nomComplet).join(', ');
  }

  initiale(livre: Livre): string {
    return livre.titre?.trim().charAt(0).toUpperCase() || '?';
  }

  // Identique à la logique du catalogue : même clé de hash → même accent
  accent(livre: Livre): string {
    const cle = livre.categorie || 'Non classé';
    let hash = 0;
    for (let i = 0; i < cle.length; i++) {
      hash = (hash * 31 + cle.charCodeAt(i)) >>> 0;
    }
    return ACCENTS[hash % ACCENTS.length];
  }

  // Cote de classification façon fiche de bibliothèque : 3 lettres de la
  // catégorie + identifiant, ex. "SCI-014"
  cote(livre: Livre): string {
    const prefixe = (livre.categorie || 'GEN')
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
      .toUpperCase().replace(/[^A-Z]/g, '').slice(0, 3) || 'GEN';
    return `${prefixe}-${String(livre.id).padStart(3, '0')}`;
  }

  private exigeConnexion(): boolean {
    if (this.auth.isLoggedIn()) return true;
    this.ui.error('Connectez-vous pour emprunter ou réserver');
    this.router.navigate(['/login']);
    return false;
  }

  private rafraichir(id: number): void {
    this.livreService.detail(id).subscribe(l => { this.livre.set(l); this.action.set(false); });
  }
}