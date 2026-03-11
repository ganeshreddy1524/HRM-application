import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-performance',
  template: `
  <div class="row g-3">
    <div class="col-lg-5" *ngIf="canCreateReview()">
      <div class="card p-3">
        <h5>{{ auth.role() === 'ADMIN' ? 'Admin Review' : 'Manager Review for Team Member' }}</h5>
        <form class="rwf-form" [formGroup]="form" (ngSubmit)="submit()">
          <select class="form-select mb-2" formControlName="employeeId">
            <option value="">{{ auth.role() === 'ADMIN' ? 'Select Employee or Manager' : 'Select Employee' }}</option>
            <option *ngFor="let e of employees" [value]="e.id">{{ e.fullName }} ({{ e.employeeId || e.email }})</option>
          </select>
          <textarea class="form-control mb-2" placeholder="Key deliverables" formControlName="keyDeliverables"></textarea>
          <textarea class="form-control mb-2" placeholder="Accomplishments" formControlName="accomplishments"></textarea>
          <textarea class="form-control mb-2" placeholder="Areas of improvement" formControlName="areasOfImprovement"></textarea>
          <input type="number" min="1" max="5" class="form-control mb-2" formControlName="selfRating" placeholder="Rating (1-5)">
          <div class="text-success small mb-2" *ngIf="successMsg">{{ successMsg }}</div>
          <div class="text-danger small mb-2" *ngIf="errorMsg">{{ errorMsg }}</div>
          <button class="btn btn-primary w-100" [disabled]="form.invalid">Submit Review</button>
        </form>
      </div>
    </div>

    <div [class]="canCreateReview() ? 'col-lg-7' : 'col-lg-12'">
      <div class="card p-3">
        <h5>{{ canCreateReview() ? 'Submitted Reviews' : 'My Performance Reports' }}</h5>

        <div class="rwf-review-summary mb-3" *ngIf="!canCreateReview() && reviews.length > 0">
          <div class="rwf-pie" [ngStyle]="pieStyle(averageRating())">
            <span>{{ averageRating().toFixed(1) }}/5</span>
          </div>
          <div>
            <div class="rwf-stars mb-1">{{ stars(averageRating()) }}</div>
            <small class="text-muted">Average manager rating across {{ reviews.length }} review(s)</small>
          </div>
        </div>

        <div *ngFor="let r of reviews" class="border rounded p-2 mb-2">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div><strong>Employee:</strong> {{ r.employeeName || '-' }}</div>
              <div><strong>Status:</strong> {{ r.status }}</div>
              <div class="rwf-stars">{{ stars(reviewRating(r)) }}</div>
            </div>
            <button class="btn btn-sm btn-outline-primary" (click)="viewDetails(r)">View Details</button>
          </div>
        </div>

        <div class="text-muted" *ngIf="reviews.length === 0">No performance reports found.</div>
      </div>

      <div class="card p-3 mt-3" *ngIf="selectedReview">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h6 class="mb-0">Performance Review Details</h6>
          <button type="button" class="rwf-close-btn" (click)="selectedReview = null">x</button>
        </div>
        <div><strong>Employee:</strong> {{ selectedReview.employeeName || '-' }}</div>
        <div><strong>Status:</strong> {{ selectedReview.status }}</div>
        <div><strong>Manager/Admin Rating:</strong> {{ selectedReview.managerRating || '-' }}</div>
        <hr>
        <div><strong>Key Deliverables:</strong><br>{{ selectedReview.keyDeliverables || '-' }}</div>
        <div class="mt-2"><strong>Accomplishments:</strong><br>{{ selectedReview.accomplishments || '-' }}</div>
        <div class="mt-2"><strong>Areas of Improvement:</strong><br>{{ selectedReview.areasOfImprovement || '-' }}</div>
        <div class="mt-2"><strong>Manager/Admin Feedback:</strong><br>{{ selectedReview.managerFeedback || '-' }}</div>
      </div>
    </div>
  </div>
  `
})
export class PerformanceComponent implements OnInit {
  reviews: any[] = [];
  employees: any[] = [];
  selectedReview: any = null;
  successMsg = '';
  errorMsg = '';
  form: FormGroup;

  constructor(private fb: FormBuilder, private api: ApiService, public auth: AuthService) {
    this.form = this.fb.group({
      employeeId: ['', Validators.required],
      keyDeliverables: ['', Validators.required],
      accomplishments: ['', Validators.required],
      areasOfImprovement: ['', Validators.required],
      selfRating: [3, [Validators.required, Validators.min(1), Validators.max(5)]]
    });
  }

  ngOnInit(): void {
    this.load();
    if (this.canCreateReview()) {
      this.loadEmployees();
    }
  }

  canCreateReview(): boolean {
    const role = this.auth.role();
    return role === 'MANAGER' || role === 'ADMIN';
  }

  load(): void {
    const endpoint = this.canCreateReview() ? '/performance/team' : '/performance/my';
    this.api.get<any>(endpoint).subscribe(r => {
      this.reviews = Array.isArray(r) ? r : (r?.reviews || []);
      if (this.selectedReview) {
        this.selectedReview = this.reviews.find(x => x.id === this.selectedReview.id) || null;
      }
    });
  }

  loadEmployees(): void {
    if (this.auth.role() === 'ADMIN') {
      this.api.get<any[]>('/employees').subscribe({
        next: r => this.employees = r.filter(x => x.roleId === 1 || x.roleId === 2),
        error: () => this.employees = []
      });
      return;
    }

    this.api.get<any[]>('/employees/team').subscribe({
      next: r => {
        if (r?.length) {
          this.employees = r;
        } else {
          this.api.get<any[]>('/employees/search?q=').subscribe({
            next: all => this.employees = all,
            error: () => this.employees = []
          });
        }
      },
      error: () => {
        this.api.get<any[]>('/employees/search?q=').subscribe({
          next: all => this.employees = all,
          error: () => this.employees = []
        });
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.successMsg = '';
    this.errorMsg = '';
    this.api.post('/performance', this.form.value).subscribe({
      next: () => {
        this.successMsg = 'Performance review submitted.';
        this.form.reset({ employeeId: '', keyDeliverables: '', accomplishments: '', areasOfImprovement: '', selfRating: 3 });
        this.load();
      },
      error: err => this.errorMsg = err?.error?.error || 'Unable to submit review.'
    });
  }

  reviewRating(review: any): number {
    const value = Number(review?.managerRating || review?.selfRating || 0);
    return Number.isNaN(value) ? 0 : Math.max(0, Math.min(5, value));
  }

  averageRating(): number {
    if (!this.reviews.length) return 0;
    const total = this.reviews.reduce((sum, r) => sum + this.reviewRating(r), 0);
    return total / this.reviews.length;
  }

  stars(rating: number): string {
    const rounded = Math.round(Math.max(0, Math.min(5, rating)));
    return '*'.repeat(rounded) + '-'.repeat(5 - rounded);
  }

  pieStyle(rating: number): { [key: string]: string } {
    const percent = Math.round((Math.max(0, Math.min(5, rating)) / 5) * 100);
    return {
      background: `conic-gradient(var(--rwf-primary) 0% ${percent}%, #ffe8cc ${percent}% 100%)`
    };
  }

  viewDetails(review: any): void {
    this.selectedReview = review;
  }
}
