import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatListModule } from '@angular/material/list';
import { NotificationService } from '../core/api.service';
import { Notification } from '../core/models';

@Component({
  selector: 'app-notification-bell',
  imports: [DatePipe, MatIconModule, MatButtonModule, MatBadgeModule, MatMenuModule, MatListModule],
  template: `
    <button mat-icon-button [matMenuTriggerFor]="menu" (menuOpened)="charger()"
            [matBadge]="nonLues() || null" matBadgeColor="warn" aria-label="Notifications">
      <mat-icon>notifications</mat-icon>
    </button>

    <mat-menu #menu="matMenu" class="notif-menu">
      <div class="notif-header" (click)="$event.stopPropagation()">
        <span>Notifications</span>
        @if (notifications().length) {
          <button mat-button (click)="toutLu()">Tout marquer lu</button>
        }
      </div>
      @if (notifications().length === 0) {
        <div class="notif-empty">Aucune notification</div>
      }
      @for (n of notifications(); track n.id) {
        <button mat-menu-item class="notif-item" [class.non-lue]="!n.lue" (click)="lire(n)">
          <mat-icon [class]="'icon-' + n.type">{{ icone(n.type) }}</mat-icon>
          <div class="notif-text">
            <div class="notif-msg">{{ n.message }}</div>
            <div class="notif-date">{{ n.dateCreation | date:'dd/MM/yyyy HH:mm' }}</div>
          </div>
        </button>
      }
    </mat-menu>
  `,
  styles: [`
    :host { display: inline-block; }
    .notif-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 8px 12px; font-weight: 600; border-bottom: 1px solid var(--mat-sys-outline-variant);
    }
    .notif-empty { padding: 24px; text-align: center; color: var(--mat-sys-on-surface-variant); }
    .notif-item { height: auto; white-space: normal; }
    .notif-item .notif-text { display: inline-block; padding: 6px 0; }
    .notif-msg { font-size: 13px; line-height: 1.3; }
    .notif-date { font-size: 11px; color: var(--mat-sys-on-surface-variant); margin-top: 2px; }
    .non-lue { background: color-mix(in srgb, var(--mat-sys-primary) 8%, transparent); }
    .icon-RETARD { color: #d32f2f; }
    .icon-ECHEANCE_PROCHE { color: #ed6c02; }
    .icon-RESERVATION_DISPONIBLE { color: #2e7d32; }
    .icon-INFO { color: #0288d1; }
  `],
})
export class NotificationBell implements OnInit, OnDestroy {
  private service = inject(NotificationService);

  nonLues = signal(0);
  notifications = signal<Notification[]>([]);
  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.rafraichirCompteur();
    this.timer = setInterval(() => this.rafraichirCompteur(), 30000);
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  rafraichirCompteur(): void {
    this.service.compteNonLues().subscribe({
      next: r => this.nonLues.set(r.count),
      error: () => {},
    });
  }

  charger(): void {
    this.service.mesNotifications().subscribe(list => this.notifications.set(list));
  }

  lire(n: Notification): void {
    if (n.lue) return;
    this.service.marquerLue(n.id).subscribe(() => {
      n.lue = true;
      this.notifications.update(l => [...l]);
      this.rafraichirCompteur();
    });
  }

  toutLu(): void {
    this.service.marquerToutLu().subscribe(() => {
      this.notifications.update(l => l.map(n => ({ ...n, lue: true })));
      this.nonLues.set(0);
    });
  }

  icone(type: Notification['type']): string {
    switch (type) {
      case 'RETARD': return 'error';
      case 'ECHEANCE_PROCHE': return 'schedule';
      case 'RESERVATION_DISPONIBLE': return 'check_circle';
      default: return 'info';
    }
  }
}
