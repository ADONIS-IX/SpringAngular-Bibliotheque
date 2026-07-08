import { Component, inject } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
    titre: string;
    message: string;
    detail?: string;
    confirmLabel?: string;
    danger?: boolean;
}

@Component({
    selector: 'app-confirm-dialog',
    imports: [MatDialogModule, MatButtonModule, MatIconModule],
    template: `
    <div class="confirm-head">
      <span class="badge" [class.badge--danger]="data.danger">
        <mat-icon>{{ data.danger ? 'delete_forever' : 'help_outline' }}</mat-icon>
      </span>
      <h2 mat-dialog-title>{{ data.titre }}</h2>
    </div>

    <mat-dialog-content>
      <p class="message">{{ data.message }}</p>
      @if (data.detail) {
        <p class="detail">{{ data.detail }}</p>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false" class="btn-ghost">Annuler</button>
      <button mat-flat-button [mat-dialog-close]="true" [class.btn-danger]="data.danger" [class.btn-primary]="!data.danger">
        {{ data.confirmLabel ?? 'Confirmer' }}
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Source+Serif+4:wght@600;700&family=Inter:wght@400;500;600;700&display=swap');

    :host {
      --ink: #0f1222;
      --ink-soft: #6b7280;
      --indigo: #4f46e5;
      --indigo-soft: #eef0ff;
      --red: #b91c1c;
      --red-soft: #fdecec;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    }

    .confirm-head {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 24px 24px 4px;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--indigo-soft);
      flex-shrink: 0;
    }
    .badge mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: var(--indigo);
    }
    .badge--danger {
      background: var(--red-soft);
    }
    .badge--danger mat-icon {
      color: var(--red);
    }

    h2[mat-dialog-title] {
      margin: 0;
      font-family: 'Source Serif 4', Georgia, serif;
      font-weight: 700;
      font-size: 19px;
      color: var(--ink);
    }

    mat-dialog-content {
      padding: 4px 24px 8px !important;
      min-width: 320px;
    }
    .message {
      margin: 0 0 4px;
      font-size: 14px;
      color: var(--ink);
      line-height: 1.5;
    }
    .detail {
      margin: 0;
      font-size: 12.5px;
      color: var(--ink-soft);
      line-height: 1.5;
    }

    mat-dialog-actions {
      padding: 14px 24px 20px !important;
    }
    .btn-ghost {
      color: var(--ink-soft) !important;
    }
    .btn-primary {
      background: var(--indigo) !important;
      color: #fff !important;
    }
    .btn-danger {
      background: var(--red) !important;
      color: #fff !important;
    }

    @media (max-width: 420px) {
      mat-dialog-content { min-width: 240px; }
    }
  `],
})
export class ConfirmDialog {
    data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}