import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { CreateUserRequest, UpdateUserRequest, Role, Utilisateur } from '../../core/models';

export interface UtilisateurDialogData {
  mode: 'create' | 'edit';
  utilisateur?: Utilisateur;
}

@Component({
  selector: 'app-utilisateur-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatSlideToggleModule, MatButtonModule,
  ],
  template: `
    <div class="dialog-head">
      <p class="eyebrow">{{ isCreate ? 'Nouveau compte' : 'Modification' }}</p>
      <h2 mat-dialog-title>{{ isCreate ? 'Créer un utilisateur' : (data.utilisateur?.nom ?? 'Modifier l\\'utilisateur') }}</h2>
    </div>

    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nom</mat-label>
          <input matInput formControlName="nom" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Mot de passe</mat-label>
          <input matInput type="password" formControlName="password" />
          @if (!isCreate) {
            <mat-hint>Laissez vide pour conserver le mot de passe actuel.</mat-hint>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Rôle</mat-label>
          <mat-select formControlName="role">
            @for (option of roleOptions; track option.value) {
              <mat-option [value]="option.value" [disabled]="option.disabled">
                {{ option.label }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>

        @if (!isCreate) {
          <mat-slide-toggle formControlName="actif" class="toggle">Compte actif</mat-slide-toggle>
        }
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="fermer()" class="btn-ghost">Annuler</button>
      <button mat-flat-button class="btn-primary" [disabled]="form.invalid" (click)="valider()">
        {{ isCreate ? 'Créer' : 'Modifier' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Source+Serif+4:wght@600;700&family=Inter:wght@400;500;600;700&display=swap');

    :host {
      --ink: #0f1222;
      --ink-soft: #6b7280;
      --indigo: #4f46e5;
      --gold: #b8842f;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    }

    .dialog-head {
      padding: 24px 24px 4px;
    }
    .eyebrow {
      margin: 0 0 4px;
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--gold);
    }
    h2[mat-dialog-title] {
      margin: 0;
      font-family: 'Source Serif 4', Georgia, serif;
      font-weight: 700;
      font-size: 21px;
      color: var(--ink);
    }

    .dialog-form {
      display: flex;
      flex-direction: column;
      min-width: 420px;
      padding-top: 12px;
    }
    .full { width: 100%; }
    .toggle { margin: 4px 0 8px; }

    mat-dialog-actions {
      padding: 12px 24px 20px !important;
    }
    .btn-ghost {
      color: var(--ink-soft) !important;
    }
    .btn-primary {
      background: var(--indigo) !important;
      color: #fff !important;
    }

    @media (max-width: 480px) {
      .dialog-form { min-width: 260px; }
    }
  `],
})
export class UtilisateurDialog {
  private dialogRef = inject(MatDialogRef<UtilisateurDialog>);
  private fb = inject(FormBuilder);
  data = inject(MAT_DIALOG_DATA) as UtilisateurDialogData;

  readonly isCreate = this.data.mode === 'create';
  readonly roleOptions = [
    { value: 'ETUDIANT', label: 'Étudiant' },
    { value: 'BIBLIOTHECAIRE', label: 'Bibliothécaire' },
    ...(this.data.utilisateur?.role === 'ADMIN' ? [{ value: 'ADMIN', label: 'Admin', disabled: true }] : []),
  ];

  form = this.fb.nonNullable.group({
    nom: [this.data.utilisateur?.nom ?? '', Validators.required],
    email: [this.data.utilisateur?.email ?? '', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(6)]],
    role: [this.data.utilisateur?.role ?? 'ETUDIANT', Validators.required],
    actif: [this.data.utilisateur?.actif ?? true],
  });

  constructor() {
    if (!this.isCreate && this.data.utilisateur?.role === 'ADMIN') {
      this.form.controls.role.disable();
      this.form.controls.password.clearValidators();
      this.form.controls.password.updateValueAndValidity();
    }
  }

  fermer(): void {
    this.dialogRef.close();
  }

  valider(): void {
    if (this.form.invalid) return;
    const rawValue = this.form.getRawValue();

    if (this.isCreate) {
      this.dialogRef.close(rawValue as CreateUserRequest);
      return;
    }

    const update: UpdateUserRequest = {
      nom: rawValue.nom,
      email: rawValue.email,
      password: rawValue.password || undefined,
      role: rawValue.role as Role,
      actif: rawValue.actif,
    };
    this.dialogRef.close(update);
  }
}