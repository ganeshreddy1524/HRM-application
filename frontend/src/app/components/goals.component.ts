import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-goals',
  template: `
  <div class="row g-3">
    <div class="col-lg-4">
      <div class="card p-3">
        <h5>Create Goal</h5>
        <form class="rwf-form" [formGroup]="form" (ngSubmit)="submit()">
          <textarea class="form-control mb-2" formControlName="description" placeholder="Goal description"></textarea>
          <input type="date" class="form-control mb-2" formControlName="deadline">
          <select class="form-select mb-2" formControlName="priority">
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
          <div class="text-success small mb-2" *ngIf="successMsg">{{ successMsg }}</div>
          <div class="text-danger small mb-2" *ngIf="errorMsg">{{ errorMsg }}</div>
          <button class="btn btn-primary w-100" [disabled]="form.invalid">Save Goal</button>
        </form>
      </div>
    </div>
    <div class="col-lg-8">
      <div class="card p-3">
        <h5>My Goals</h5>
        <table class="table table-sm">
          <thead><tr><th>Description</th><th>Deadline</th><th>Priority</th><th>Status</th><th>Update</th></tr></thead>
          <tbody>
            <tr *ngFor="let g of goals">
              <td>{{ g.description }}</td>
              <td>{{ g.deadline }}</td>
              <td>{{ g.priority }}</td>
              <td>{{ g.status }}</td>
              <td>
                <select class="form-select form-select-sm" [value]="g.status" [disabled]="g.status==='COMPLETED'"
                        (change)="updateStatus(g.id, $any($event.target).value)">
                  <option value="NOT_STARTED">NOT_STARTED</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="COMPLETED">COMPLETED</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  `
})
export class GoalsComponent implements OnInit {
  goals: any[] = [];
  form: FormGroup;
  successMsg = '';
  errorMsg = '';

  constructor(private fb: FormBuilder, private api: ApiService) {
    this.form = this.fb.group({
      description: ['', Validators.required],
      deadline: ['', Validators.required],
      priority: ['MEDIUM', Validators.required]
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void { this.api.get<any[]>('/goals/my').subscribe(r => this.goals = r); }

  submit(): void {
    if (this.form.invalid) return;
    this.successMsg = '';
    this.errorMsg = '';
    this.api.post('/goals', this.form.value).subscribe({
      next: () => {
        this.successMsg = 'Goal created successfully.';
        this.form.reset({ description: '', deadline: '', priority: 'MEDIUM' });
        this.load();
      },
      error: (err) => {
        const backendError = err?.error;
        if (backendError?.error) {
          this.errorMsg = backendError.error;
        } else if (backendError && typeof backendError === 'object') {
          const first = Object.values(backendError)[0] as string;
          this.errorMsg = first || 'Unable to create goal.';
        } else {
          this.errorMsg = 'Unable to create goal.';
        }
      }
    });
  }

  updateStatus(id: number, status: string): void {
    this.successMsg = '';
    this.errorMsg = '';
    this.api.patch(`/goals/${id}/status`, { status }).subscribe({
      next: () => {
        this.successMsg = 'Goal status updated.';
        this.load();
      },
      error: (err) => this.errorMsg = err?.error?.error || 'Unable to update goal status.'
    });
  }
}
