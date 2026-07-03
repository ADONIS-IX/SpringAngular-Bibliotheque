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
  selector: 'app-register',
  imports: [
    ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressBarModule,
  ],
  templateUrl: './register.html',
  styleUrl: '../login/auth.scss',
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private ui = inject(Ui);

  loading = signal(false);

  form = this.fb.nonNullable.group({
    nom: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const { nom, email, password } = this.form.getRawValue();
    this.auth.register(nom, email, password).subscribe({
      next: () => {
        this.ui.success('Compte créé, bienvenue !');
        this.router.navigate(['/catalogue']);
      },
      error: err => {
        this.loading.set(false);
        this.ui.error(err);
      },
    });
  }
}
