import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from './core/auth.service';
import { NotificationBell } from './shared/notification-bell';

interface NavLink { path: string; label: string; icon: string; biblio?: boolean; }

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatListModule, MatIconModule,
    MatButtonModule, MatMenuModule, NotificationBell,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly user = this.auth.user;
  readonly isLoggedIn = this.auth.isLoggedIn;
  readonly isBiblio = this.auth.isBibliothecaire;
  opened = signal(true);

  private readonly liens: NavLink[] = [
    { path: '/catalogue', label: 'Catalogue', icon: 'menu_book' },
    { path: '/mes-emprunts', label: 'Mes emprunts', icon: 'import_contacts' },
    { path: '/mes-reservations', label: 'Mes réservations', icon: 'bookmark' },
    { path: '/mes-penalites', label: 'Mes pénalités', icon: 'payments' },
    { path: '/dashboard', label: 'Tableau de bord', icon: 'dashboard', biblio: true },
    { path: '/gestion-livres', label: 'Gérer les livres', icon: 'library_books', biblio: true },
    { path: '/gestion-auteurs', label: 'Gérer les auteurs', icon: 'people', biblio: true },
    { path: '/gestion-emprunts', label: 'Emprunts & retours', icon: 'assignment_return', biblio: true },
    { path: '/gestion-penalites', label: 'Pénalités', icon: 'account_balance_wallet', biblio: true },
  ];

  readonly navLinks = computed(() => this.liens.filter(l => !l.biblio || this.isBiblio()));

  toggle(): void {
    this.opened.update(v => !v);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
