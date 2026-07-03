import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../core/auth.service';
import { Ui } from '../../core/ui';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressBarModule,
  ],
  templateUrl: './login.html',
  styleUrl: './auth.scss',
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private ui = inject(Ui);

  loading = signal(false);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  comptesDemo = [
    { label: 'Admin', email: 'admin@universite.sn', password: 'Admin2024!' },
    { label: 'Bibliothécaire', email: 'biblio@universite.sn', password: 'Biblio2024!' },
    { label: 'Étudiant', email: 'etudiant@universite.sn', password: 'Etudiant2024!' },
  ];

  remplir(c: { email: string; password: string }): void {
    this.form.setValue({ email: c.email, password: c.password });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => {
        this.ui.success('Connexion réussie');
        this.router.navigate(['/catalogue']);
      },
      error: err => {
        this.loading.set(false);
        this.ui.error(err);
      },
    });
  }
}
