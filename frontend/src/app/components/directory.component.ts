import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-directory',
  template: `
  <div class="card p-3">
    <h5>Employee Directory</h5>
    <div class="d-flex gap-2 mb-2">
      <input class="form-control" placeholder="Search by name or email" [(ngModel)]="query">
      <button class="btn btn-outline-primary" (click)="search()">Search</button>
    </div>
    <div class="text-danger small mb-2" *ngIf="errorMsg">{{ errorMsg }}</div>
    <table class="table table-sm">
      <thead><tr><th>Employee</th><th>Name</th><th>Email</th><th>Department</th><th>Designation</th></tr></thead>
      <tbody>
        <tr *ngFor="let u of users">
          <td>{{ u.employeeId || u.id }}</td>
          <td>{{ u.fullName }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.departmentName || '-' }}</td>
          <td>{{ u.designationName || '-' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
  `
})
export class DirectoryComponent implements OnInit {
  users: any[] = [];
  query = '';
  errorMsg = '';

  constructor(private api: ApiService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    const q = this.query || '';
    this.errorMsg = '';
    this.api.get<any[]>(`/employees/search?q=${encodeURIComponent(q)}`).subscribe({
      next: r => this.users = r,
      error: err => {
        this.users = [];
        this.errorMsg = err?.error?.error || 'Unable to load directory.';
      }
    });
  }
}
