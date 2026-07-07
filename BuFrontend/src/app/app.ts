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

interface NavLink { path: string; label: string; icon: string; auth?: boolean; etudiant?: boolean; biblio?: boolean; admin?: boolean; public?: boolean; }

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
  readonly isAdmin = this.auth.isAdmin;
  opened = signal(true);

  private readonly liens: NavLink[] = [
    { path: '/catalogue', label: 'Catalogue', icon: 'menu_book', public: true },
    { path: '/mes-emprunts', label: 'Mes emprunts', icon: 'import_contacts', auth: true, etudiant: true },
    { path: '/mes-reservations', label: 'Mes réservations', icon: 'bookmark', auth: true, etudiant: true },
    { path: '/mes-penalites', label: 'Mes pénalités', icon: 'payments', auth: true, etudiant: true },
    { path: '/dashboard', label: 'Tableau de bord', icon: 'dashboard', auth: true, biblio: true },
    { path: '/gestion-livres', label: 'Gérer les livres', icon: 'library_books', auth: true, biblio: true },
    { path: '/gestion-auteurs', label: 'Gérer les auteurs', icon: 'people', auth: true, biblio: true },
    { path: '/gestion-emprunts', label: 'Emprunts & retours', icon: 'assignment_return', auth: true, biblio: true },
    { path: '/gestion-penalites', label: 'Pénalités', icon: 'account_balance_wallet', auth: true, biblio: true },
    { path: '/gestion-utilisateurs', label: 'Gérer les utilisateurs', icon: 'manage_accounts', auth: true, admin: true },
  ];

  readonly navLinks = computed(() => this.liens.filter(link => {
    if (link.public) {
      return true;
    }
    if (link.admin) {
      return this.isAdmin();
    }
    if (link.biblio) {
      return this.isBiblio();
    }
    if (link.etudiant) {
      return this.isLoggedIn() && !this.isBiblio();
    }
    return link.auth ? this.isLoggedIn() : true;
  }));

  toggle(): void {
    this.opened.update(v => !v);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
