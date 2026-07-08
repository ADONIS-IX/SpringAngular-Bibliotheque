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
    <div class="dialog-head">
      <p class="eyebrow">{{ data ? 'Modification' : 'Nouvel auteur' }}</p>
      <h2 mat-dialog-title>{{ data ? (data.prenom + ' ' + data.nom) : 'Ajouter un auteur' }}</h2>
    </div>

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
      <button mat-button mat-dialog-close class="btn-ghost">Annuler</button>
      <button mat-flat-button class="btn-primary" [disabled]="form.invalid" (click)="valider()">
        Enregistrer
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
      min-width: 340px;
      padding-top: 12px;
    }
    .full { width: 100%; }

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

    @media (max-width: 400px) {
      .dialog-form { min-width: 240px; }
    }
  `],
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