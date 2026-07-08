import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from './core/auth.service';
import { NotificationBell } from './shared/notification-bell';

interface NavLink {
  path: string;
  label: string;
  icon: string;
  auth?: boolean;
  etudiant?: boolean;
  biblio?: boolean;
  admin?: boolean;
  public?: boolean;
  children?: NavLink[];
}

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    NotificationBell,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly user = this.auth.user;
  readonly isLoggedIn = this.auth.isLoggedIn;
  readonly isBiblio = this.auth.isBibliothecaire;
  readonly isAdmin = this.auth.isAdmin;

  // Sidebar réduit au démarrage
  opened = signal(false);
  isHovered = signal(false);

  private submenusState = signal<Record<string, boolean>>({});

  isLoginPage = signal(window.location.pathname === '/login');

  // Initiales utilisées par le badge avatar de la navbar (ex: "Awa Diop" -> "AD")
  readonly initials = computed(() => {
    const nom = this.user()?.nom ?? '';
    return nom
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('');
  });

  private readonly liens: NavLink[] = [
    { path: '/catalogue', label: 'Catalogue', icon: 'bx-book', public: true },
    { path: '/mes-emprunts', label: 'Mes emprunts', icon: 'bx-import', auth: true, etudiant: true },
    { path: '/mes-reservations', label: 'Mes réservations', icon: 'bx-bookmark', auth: true, etudiant: true },
    { path: '/mes-penalites', label: 'Mes pénalités', icon: 'bx-money', auth: true, etudiant: true },
    {
      path: '/gestion',
      label: 'Gestion',
      icon: 'bx-cog',
      auth: true,
      biblio: true,
      children: [
        { path: '/gestion-livres', label: 'Livres', icon: 'bx-book' },
        { path: '/gestion-auteurs', label: 'Auteurs', icon: 'bx-user' },
        { path: '/gestion-emprunts', label: 'Emprunts & retours', icon: 'bx-transfer' },
        { path: '/gestion-penalites', label: 'Pénalités', icon: 'bx-wallet' },
      ],
    },
    { path: '/dashboard', label: 'Tableau de bord', icon: 'bx-grid-alt', auth: true, biblio: true },
    { path: '/gestion-utilisateurs', label: 'Gérer les utilisateurs', icon: 'bx-group', auth: true, admin: true },
  ];

  readonly navLinks = computed(() =>
    this.liens.filter((link) => {
      if (link.public) return true;
      if (link.admin) return this.isAdmin();
      if (link.biblio) return this.isBiblio();
      if (link.etudiant) return this.isLoggedIn() && !this.isBiblio();
      return link.auth ? this.isLoggedIn() : true;
    })
  );

  ngOnInit() {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.isLoginPage.set(this.router.url === '/login');
      });
  }

  toggleSubmenu(path: string) {
    this.submenusState.update((state) => ({
      ...state,
      [path]: !state[path],
    }));
  }

  isSubmenuOpen(path: string): boolean {
    return this.submenusState()[path] || false;
  }

  collapseSidebar() {
    this.opened.set(false);
  }

  expandSidebar() {
    this.opened.set(true);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}