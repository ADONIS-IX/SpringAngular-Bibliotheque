import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CreateUserRequest } from '../../core/models';

@Component({
  selector: 'app-utilisateur-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Créer un utilisateur</h2>
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
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Rôle</mat-label>
          <mat-select formControlName="role">
            <mat-option value="ETUDIANT">Étudiant</mat-option>
            <mat-option value="BIBLIOTHECAIRE">Bibliothécaire</mat-option>
            <mat-option value="ADMIN">Admin</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="fermer()">Annuler</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="valider()">Créer</button>
    </mat-dialog-actions>
  `,
})
export class UtilisateurDialog {
  private dialogRef = inject(MatDialogRef<UtilisateurDialog>);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['ETUDIANT', Validators.required],
  });

  fermer(): void {
    this.dialogRef.close();
  }

  valider(): void {
    if (this.form.invalid) return;
    this.dialogRef.close(this.form.getRawValue() as CreateUserRequest);
  }
}
