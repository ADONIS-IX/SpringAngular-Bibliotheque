import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Auteur, Livre, LivreRequest } from '../../core/models';

interface DialogData {
  livre: Livre | null;
  auteurs: Auteur[];
}

@Component({
  selector: 'app-livre-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule,
  ],
  template: `
    <div class="dialog-head">
      <p class="eyebrow">{{ data.livre ? 'Modification' : 'Nouvel ouvrage' }}</p>
      <h2 mat-dialog-title>{{ data.livre ? data.livre.titre : 'Ajouter un livre' }}</h2>
    </div>

    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Titre</mat-label>
          <input matInput formControlName="titre" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Auteur(s)</mat-label>
          <mat-select formControlName="auteurIds" multiple>
            @for (a of data.auteurs; track a.id) {
              <mat-option [value]="a.id">{{ a.prenom }} {{ a.nom }}</mat-option>
            }
          </mat-select>
          <mat-hint>Un livre peut avoir plusieurs auteurs</mat-hint>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>ISBN</mat-label>
            <input matInput formControlName="isbn" maxlength="13" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Année</mat-label>
            <input matInput type="number" formControlName="anneePublication" />
          </mat-form-field>
        </div>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Catégorie</mat-label>
            <input matInput formControlName="categorie" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Stock total</mat-label>
            <input matInput type="number" formControlName="stockTotal" min="0" />
          </mat-form-field>
        </div>
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
      --line: #e8eaf0;
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
      min-width: 440px;
      padding-top: 12px;
    }
    .full { width: 100%; }
    .row { display: flex; gap: 12px; }
    .row mat-form-field { flex: 1; }

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

    @media (max-width: 520px) {
      .dialog-form { min-width: 260px; }
      .row { flex-direction: column; gap: 0; }
    }
  `],
})
export class LivreDialog {
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<LivreDialog>);
  data = inject<DialogData>(MAT_DIALOG_DATA);

  form = this.fb.nonNullable.group({
    titre: [this.data.livre?.titre ?? '', Validators.required],
    isbn: [this.data.livre?.isbn ?? ''],
    anneePublication: [this.data.livre?.anneePublication ?? null as number | null],
    categorie: [this.data.livre?.categorie ?? ''],
    stockTotal: [this.data.livre?.stockTotal ?? 1, [Validators.required, Validators.min(0)]],
    auteurIds: [this.data.livre?.auteurs.map(a => a.id) ?? [] as number[],
      [Validators.required, Validators.minLength(1)]],
  });

  valider(): void {
    if (this.form.invalid) return;
    this.ref.close(this.form.getRawValue() as LivreRequest);
  }
}