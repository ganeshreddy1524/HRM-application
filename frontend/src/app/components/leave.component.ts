import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-leave',
  template: `
  <div class="row g-3">
    <div class="col-12">
      <div class="card p-3">
        <div class="row text-center">
          <div class="col-md-4 mb-2 mb-md-0">
            <h6 class="mb-1">Total Leaves</h6>
            <h4 class="mb-0">{{ summary.totalLeaves }}</h4>
          </div>
          <div class="col-md-4 mb-2 mb-md-0">
            <h6 class="mb-1">Remaining Leaves</h6>
            <h4 class="mb-0">{{ summary.remainingLeaves }}</h4>
          </div>
          <div class="col-md-4">
            <h6 class="mb-1">Pending For Approval</h6>
            <h4 class="mb-0">{{ summary.pendingApprovals }}</h4>
          </div>
        </div>
      </div>
    </div>
    <div class="col-lg-4">
      <div class="card p-3">
        <h5>Apply Leave</h5>
        <form class="rwf-form" [formGroup]="form" (ngSubmit)="submit()">
          <select class="form-select mb-2" formControlName="leaveType">
            <option value="CASUAL">Casual Leave</option>
            <option value="SICK">Sick Leave</option>
            <option value="PAID">Paid Leave</option>
          </select>
          <input type="date" class="form-control mb-2" formControlName="startDate">
          <input type="date" class="form-control mb-2" formControlName="endDate">
          <textarea class="form-control mb-2" placeholder="Reason" formControlName="reason"></textarea>
          <div class="text-warning small mb-2" *ngIf="summary.remainingLeaves === 0">
            No leave balance remaining. You cannot apply for leave.
          </div>
          <div class="text-success small mb-2" *ngIf="successMsg">{{ successMsg }}</div>
          <div class="text-danger small mb-2" *ngIf="errorMsg">{{ errorMsg }}</div>
          <button class="btn btn-primary w-100" [disabled]="form.invalid || summary.remainingLeaves === 0">Submit</button>
        </form>
      </div>
    </div>
    <div class="col-lg-8">
      <div class="card p-3">
        <h5>My Leaves</h5>
        <table class="table table-sm">
          <thead><tr><th>Type</th><th>Dates</th><th>Status</th><th>Action</th></tr></thead>
          <tbody>
            <tr *ngFor="let l of leaves">
              <td>{{ l.leaveType }}</td>
              <td>{{ l.startDate }} - {{ l.endDate }}</td>
              <td>{{ l.status }}</td>
              <td><button class="btn btn-sm btn-outline-danger" *ngIf="l.status==='PENDING'" (click)="cancel(l.id)">Cancel</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  `
})
export class LeaveComponent implements OnInit {
  leaves: any[] = [];
  summary = {
    totalLeaves: 24,
    remainingLeaves: 24,
    pendingApprovals: 0
  };
  form: FormGroup;
  successMsg = '';
  errorMsg = '';

  constructor(private fb: FormBuilder, private api: ApiService) {
    this.form = this.fb.group({
      leaveType: ['CASUAL', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', Validators.required]
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.get<any[]>('/leaves/my').subscribe(r => this.leaves = r);
    this.api.get<any>('/leaves/summary').subscribe({
      next: r => {
        this.summary.totalLeaves = r?.totalAllocated ?? 24;
        this.summary.remainingLeaves = r?.remaining ?? 24;
        this.summary.pendingApprovals = r?.pending ?? 0;
      },
      error: () => {
        this.summary.totalLeaves = 24;
        this.summary.remainingLeaves = 24;
        this.summary.pendingApprovals = 0;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.successMsg = '';
    this.errorMsg = '';
    this.api.post('/leaves', this.form.value).subscribe({
      next: () => {
        this.successMsg = 'Leave request submitted successfully.';
        this.form.reset({ leaveType: 'CASUAL', startDate: '', endDate: '', reason: '' });
        this.load();
      },
      error: (err) => {
        const backendError = err?.error;
        if (backendError?.error) {
          this.errorMsg = backendError.error;
        } else if (backendError && typeof backendError === 'object') {
          const first = Object.values(backendError)[0] as string;
          this.errorMsg = first || 'Unable to submit leave request.';
        } else {
          this.errorMsg = 'Unable to submit leave request.';
        }
      }
    });
  }

  cancel(id: number): void {
    this.successMsg = '';
    this.errorMsg = '';
    this.api.patch(`/leaves/${id}/cancel`, {}).subscribe({
      next: () => {
        this.successMsg = 'Leave request canceled.';
        this.load();
      },
      error: (err) => this.errorMsg = err?.error?.error || 'Unable to cancel leave request.'
    });
  }
}
