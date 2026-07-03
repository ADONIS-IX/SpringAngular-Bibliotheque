import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Auteur, AuteurRequest } from '../../core/models';

@Component({
  selector: 'app-auteur-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Modifier' : 'Nouvel' }} auteur</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nom</mat-label>
          <input matInput formControlName="nom" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Prénom</mat-label>
          <input matInput formControlName="prenom" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nationalité</mat-label>
          <input matInput formControlName="nationalite" />
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="valider()">Enregistrer</button>
    </mat-dialog-actions>
  `,
  styles: [`.dialog-form { display: flex; flex-direction: column; min-width: 320px; padding-top: 8px; } .full { width: 100%; }`],
})
export class AuteurDialog {
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<AuteurDialog>);
  data = inject<Auteur | null>(MAT_DIALOG_DATA);

  form = this.fb.nonNullable.group({
    nom: [this.data?.nom ?? '', Validators.required],
    prenom: [this.data?.prenom ?? '', Validators.required],
    nationalite: [this.data?.nationalite ?? ''],
  });

  valider(): void {
    if (this.form.invalid) return;
    this.ref.close(this.form.getRawValue() as AuteurRequest);
  }
}
