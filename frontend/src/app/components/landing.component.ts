import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { UiStateService } from '../services/ui-state.service';

@Component({
  selector: 'app-landing',
  template: `
  <section class="landing-wrap">
    <div class="landing-content">
      <div class="landing-copy">
        <p class="landing-kicker">Welcome to RevWorkforce</p>
        <h1>One HRM platform for your organization</h1>
        <p class="landing-sub">
          Manage employee records, leave workflows, performance reviews, annual goals,
          and HR administration with secure role-based access.
        </p>
        <div class="trust-strip">
          <div class="trust-item">
            <span class="trust-dot"></span>
            <div>
              <strong>Secure Access</strong>
              <small>JWT authentication with role-based controls</small>
            </div>
          </div>
          <div class="trust-item">
            <span class="trust-dot"></span>
            <div>
              <strong>Reliable Workflow</strong>
              <small>Leave, goals, and performance in one unified platform</small>
            </div>
          </div>
          <div class="trust-item">
            <span class="trust-dot"></span>
            <div>
              <strong>Audit Friendly</strong>
              <small>Structured records for operational and HR reporting</small>
            </div>
          </div>
        </div>
      </div>

      <div class="landing-hero-art">
        <img src="assets/hr-hero.svg" alt="HR dashboard illustration" />
      </div>

      <div class="landing-panels">
        <div class="feature-tile">
          <h5>About RevWorkforce</h5>
          <p>
            RevWorkforce is your organization’s HRM application for employees, managers,
            and admins. It centralizes leave, reviews, goals, and announcements.
          </p>
        </div>
        <div class="feature-tile">
          <h5>Core Modules</h5>
          <ul>
            <li>Authentication and secure role access</li>
            <li>Leave request and approval workflows</li>
            <li>Performance reviews and manager feedback</li>
            <li>Goal planning and progress tracking</li>
            <li>Admin configuration and HR reporting</li>
          </ul>
        </div>
      </div>
    </div>

    <aside class="login-side" [class.open]="showLoginPanel" aria-label="Login panel">
      <div class="login-side-header">
        <h4>{{ resetMode ? 'Reset Password' : 'Sign In' }}</h4>
        <button class="rwf-close-btn" (click)="closeLogin()" aria-label="Close login panel">×</button>
      </div>

      <form class="rwf-form" *ngIf="!resetMode" [formGroup]="form" (ngSubmit)="submit()">
        <div class="mb-3">
          <label class="form-label">Employee ID or Email</label>
          <input class="form-control" formControlName="username" />
        </div>
        <div class="mb-3">
          <label class="form-label">Password</label>
          <input type="password" class="form-control" formControlName="password" />
        </div>
        <div class="text-danger mb-2" *ngIf="error">{{ error }}</div>
        <button class="btn btn-primary w-100" [disabled]="form.invalid">Login</button>
        <button type="button" class="btn btn-link p-0 mt-3 rwf-link-btn" (click)="openResetMode()">Forgot password?</button>
      </form>

      <form class="rwf-form" *ngIf="resetMode" [formGroup]="resetForm" (ngSubmit)="submitReset()">
        <div class="mb-2">
          <label class="form-label">Employee ID</label>
          <input class="form-control" formControlName="employeeId" />
        </div>
        <div class="mb-2">
          <label class="form-label">Official Email</label>
          <input class="form-control" formControlName="email" />
        </div>
        <div class="mb-2">
          <label class="form-label">New Password</label>
          <input type="password" class="form-control" formControlName="newPassword" />
        </div>
        <div class="mb-2">
          <label class="form-label">Confirm New Password</label>
          <input type="password" class="form-control" formControlName="confirmPassword" />
        </div>
        <div class="text-danger mb-2" *ngIf="resetError">{{ resetError }}</div>
        <div class="text-success mb-2" *ngIf="resetSuccess">{{ resetSuccess }}</div>
        <button class="btn btn-primary w-100" [disabled]="resetForm.invalid">Reset Password</button>
        <button type="button" class="btn btn-link p-0 mt-3 rwf-link-btn" (click)="closeResetMode()">Back to login</button>
      </form>

      <small class="text-muted mt-3 d-block">Welcome to RevWorkforce.</small>
    </aside>

    <footer class="landing-footer">
      <div>
        <strong>RevWorkforce Team</strong>
        <span class="ms-2">Internal HRM platform for our organization.</span>
      </div>
      <div>© 2026 RevWorkforce</div>
    </footer>
  </section>
  `
})
export class LandingComponent implements OnInit, OnDestroy {
  showLoginPanel = false;
  resetMode = false;
  error = '';
  resetError = '';
  resetSuccess = '';
  form: FormGroup;
  resetForm: FormGroup;
  private loginPanelSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private uiState: UiStateService
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.resetForm = this.fb.group({
      employeeId: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
    this.loginPanelSub = this.uiState.loginPanel$.subscribe(open => {
      this.showLoginPanel = open;
      if (!open) {
        this.error = '';
        this.resetMode = false;
        this.resetError = '';
        this.resetSuccess = '';
      }
    });
  }

  ngOnDestroy(): void {
    this.loginPanelSub?.unsubscribe();
  }

  closeLogin(): void {
    this.uiState.closeLoginPanel();
  }

  openResetMode(): void {
    this.resetMode = true;
    this.resetError = '';
    this.resetSuccess = '';
  }

  closeResetMode(): void {
    this.resetMode = false;
    this.resetError = '';
    this.resetSuccess = '';
  }

  submit(): void {
    if (this.form.invalid) return;
    const payload = {
      username: (this.form.value.username || '').toString().trim(),
      password: (this.form.value.password || '').toString().trim()
    };
    this.auth.login(payload).subscribe({
      next: () => {
        this.uiState.closeLoginPanel();
        this.router.navigate(['/dashboard']);
      },
      error: (err) => this.error = err?.error?.error || 'Invalid credentials'
    });
  }

  submitReset(): void {
    if (this.resetForm.invalid) return;

    const newPassword = (this.resetForm.value.newPassword || '').toString().trim();
    const confirmPassword = (this.resetForm.value.confirmPassword || '').toString().trim();
    if (newPassword !== confirmPassword) {
      this.resetError = 'New password and confirm password must match.';
      this.resetSuccess = '';
      return;
    }

    const payload = {
      employeeId: (this.resetForm.value.employeeId || '').toString().trim(),
      email: (this.resetForm.value.email || '').toString().trim().toLowerCase(),
      newPassword
    };

    this.auth.resetPassword(payload).subscribe({
      next: () => {
        this.resetError = '';
        this.resetSuccess = 'Password reset successful. Please login with new password.';
        this.form.patchValue({ username: payload.email, password: '' });
      },
      error: (err) => {
        this.resetSuccess = '';
        const backendError = err?.error;
        if (backendError?.error) {
          this.resetError = backendError.error;
        } else if (backendError && typeof backendError === 'object') {
          const firstMessage = Object.values(backendError)[0] as string;
          this.resetError = firstMessage || 'Unable to reset password.';
        } else {
          this.resetError = 'Unable to reset password.';
        }
      }
    });
  }
}
