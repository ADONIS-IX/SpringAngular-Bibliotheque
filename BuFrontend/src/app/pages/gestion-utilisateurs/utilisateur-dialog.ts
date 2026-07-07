import { Component, Inject, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ titre }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
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
          <mat-hint *ngIf="!isCreate">Laissez vide pour conserver le mot de passe actuel.</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Rôle</mat-label>
          <mat-select formControlName="role">
            <mat-option *ngFor="let option of roleOptions" [value]="option.value" [disabled]="option.disabled">
              {{ option.label }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-slide-toggle formControlName="actif" *ngIf="!isCreate">Actif</mat-slide-toggle>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="fermer()">Annuler</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="valider()">{{ isCreate ? 'Créer' : 'Modifier' }}</button>
    </mat-dialog-actions>
  `,
})
export class UtilisateurDialog {
  private dialogRef = inject(MatDialogRef<UtilisateurDialog>);
  private fb = inject(FormBuilder);
  private data = inject(MAT_DIALOG_DATA) as UtilisateurDialogData;

  readonly isCreate = this.data.mode === 'create';
  readonly titre = this.isCreate ? 'Créer un utilisateur' : 'Modifier un utilisateur';
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
