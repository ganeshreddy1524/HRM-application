import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-admin',
  template: `
  <div class="row g-3">
    <div class="col-lg-4">
      <div class="card p-3 mb-3">
        <h6>Add Employee</h6>
        <form class="rwf-form" [formGroup]="empForm" (ngSubmit)="saveEmployee()">
          <input class="form-control mb-2" formControlName="employeeId" placeholder="Employee ID">
          <div class="text-danger small mb-2" *ngIf="isInvalid('employeeId')">
            <span *ngIf="empForm.get('employeeId')?.errors?.['required']">Employee ID is required.</span>
            <span *ngIf="empForm.get('employeeId')?.errors?.['pattern']">Employee ID must contain only numbers.</span>
          </div>

          <input class="form-control mb-2" formControlName="fullName" placeholder="Full Name">
          <div class="text-danger small mb-2" *ngIf="isInvalid('fullName')">
            <span *ngIf="empForm.get('fullName')?.errors?.['required']">Full name is required.</span>
            <span *ngIf="empForm.get('fullName')?.errors?.['pattern']">Name must contain letters and spaces only.</span>
          </div>

          <input class="form-control mb-2" formControlName="email" placeholder="Email">
          <div class="text-danger small mb-2" *ngIf="isInvalid('email')">
            <span *ngIf="empForm.get('email')?.errors?.['required']">Email is required.</span>
            <span *ngIf="empForm.get('email')?.errors?.['email']">Enter a valid email format.</span>
          </div>

          <input type="password" class="form-control mb-2" formControlName="password" placeholder="Password">
          <div class="text-danger small mb-2" *ngIf="isInvalid('password')">
            <span *ngIf="empForm.get('password')?.errors?.['required']">Password is required.</span>
            <span *ngIf="empForm.get('password')?.errors?.['minlength']">Password must be at least 8 characters.</span>
            <span *ngIf="empForm.get('password')?.errors?.['pattern']">Password must contain uppercase, lowercase, number, and special character.</span>
          </div>

          <select class="form-select mb-2" formControlName="role">
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
            <option value="ADMIN">Admin</option>
          </select>

          <ng-container *ngIf="empForm.get('role')?.value === 'EMPLOYEE'">
            <select class="form-select mb-2" formControlName="managerId">
              <option value="">Select Manager</option>
              <option *ngFor="let m of managers" [value]="m.id">{{ m.employeeId }} - {{ m.fullName }}</option>
            </select>
            <div class="text-danger small mb-2" *ngIf="isInvalid('managerId')">
              <span *ngIf="empForm.get('managerId')?.errors?.['required']">Manager is required.</span>
            </div>
            <div class="text-muted small mb-2" *ngIf="managers.length === 0">
              No managers available. Create a manager first.
            </div>
          </ng-container>

          <div class="text-danger small mb-2" *ngIf="createEmpError">{{ createEmpError }}</div>
          <button type="submit" class="btn btn-sm btn-primary w-100">Create Employee</button>
        </form>
      </div>

      <div class="card p-3 mb-3">
        <h6>Add Department</h6>
        <form class="rwf-form" [formGroup]="depForm" (ngSubmit)="saveDepartment()">
          <input class="form-control mb-2" formControlName="name">
          <div class="text-danger small mb-2" *ngIf="depError">{{ depError }}</div>
          <button class="btn btn-sm btn-primary" [disabled]="savingDepartment">
            {{ savingDepartment ? 'Saving...' : 'Save' }}
          </button>
        </form>
      </div>
      <div class="card p-3 mb-3">
        <h6>Add Designation</h6>
        <form class="rwf-form" [formGroup]="desForm" (ngSubmit)="saveDesignation()">
          <input class="form-control mb-2" formControlName="name">
          <div class="text-danger small mb-2" *ngIf="desError">{{ desError }}</div>
          <button class="btn btn-sm btn-primary" [disabled]="savingDesignation">
            {{ savingDesignation ? 'Saving...' : 'Save' }}
          </button>
        </form>
      </div>
      <div class="card p-3">
        <h6>Add Announcement</h6>
        <form class="rwf-form" [formGroup]="annForm" (ngSubmit)="saveAnnouncement()">
          <input class="form-control mb-2" formControlName="title" placeholder="Title">
          <textarea class="form-control mb-2" formControlName="content" placeholder="Content"></textarea>
          <button class="btn btn-sm btn-primary">Publish</button>
        </form>
      </div>
    </div>
    <div class="col-lg-8">
      <div class="card p-3 mb-3">
        <h6>Leave Approvals</h6>
        <div *ngIf="teamLeaves.length === 0" class="text-muted">No leave requests.</div>
        <div *ngFor="let l of teamLeaves" class="border rounded p-2 mb-2">
          <div><strong>{{ l.employeeName || '-' }}</strong></div>
          <div>{{ l.leaveType }}: {{ l.startDate }} to {{ l.endDate }}</div>
          <div>Reason: {{ l.reason }}</div>
          <div class="mb-2">Status: {{ l.status }}</div>
          <textarea class="form-control mb-2"
                    [ngModel]="leaveComments[l.id] || ''"
                    (ngModelChange)="leaveComments[l.id] = $event"
                    [ngModelOptions]="{standalone: true}"
                    placeholder="Comment (required for reject)"></textarea>
          <div class="d-flex gap-2" *ngIf="l.status === 'PENDING'">
            <button class="btn btn-sm btn-success" (click)="approveLeave(l.id)">Approve</button>
            <button class="btn btn-sm btn-outline-danger" (click)="rejectLeave(l.id)">Reject</button>
          </div>
        </div>
        <div class="text-danger small" *ngIf="leaveError">{{ leaveError }}</div>
      </div>

      <div class="card p-3 mb-3">
        <h6>Employees</h6>
        <div class="table-responsive">
          <table class="table table-sm align-middle">
            <thead><tr><th>ID</th><th>Name</th><th>Role</th><th>Status</th><th>Action</th></tr></thead>
            <tbody>
              <tr *ngFor="let u of users">
                <td>{{ u.employeeId }}</td>
                <td>{{ u.fullName }}</td>
                <td>{{ u.role }}</td>
                <td>
                  <span class="badge" [class.bg-success]="u.active" [class.bg-secondary]="!u.active">
                    {{ u.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary" *ngIf="u.active" (click)="setActive(u.id, false)">Deactivate</button>
                  <button class="btn btn-sm btn-outline-success" *ngIf="!u.active" (click)="setActive(u.id, true)">Activate</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card p-3 mb-3">
        <h6>Departments</h6>
        <span class="badge bg-light text-dark me-2" *ngFor="let d of departments">{{ d.name }}</span>
      </div>
      <div class="card p-3 mb-3">
        <h6>Designations</h6>
        <span class="badge bg-light text-dark me-2" *ngFor="let d of designations">{{ d.name }}</span>
      </div>
      <div class="card p-3">
        <h6>Announcements</h6>
        <div *ngFor="let a of announcements">
          <strong>{{ a.title }}</strong>
          <p class="mb-2">{{ a.content }}</p>
        </div>
      </div>
    </div>
  </div>
  `
})
export class AdminComponent implements OnInit {
  users: any[] = [];
  employees: any[] = [];
  managers: any[] = [];
  departments: any[] = [];
  designations: any[] = [];
  announcements: any[] = [];
  teamLeaves: any[] = [];
  createEmpError = '';
  depError = '';
  desError = '';
  leaveError = '';
  savingDepartment = false;
  savingDesignation = false;
  leaveComments: { [key: number]: string } = {};
  empSubmitted = false;

  empForm: FormGroup;
  depForm: FormGroup;
  desForm: FormGroup;
  annForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder) {
    this.empForm = this.fb.group({
      employeeId: ['', [Validators.required, Validators.pattern('^[0-9]+$')]],
      fullName: ['', [Validators.required, Validators.pattern('^[A-Za-z ]+$')]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$')
      ]],
      role: ['EMPLOYEE', Validators.required],
      managerId: ['']
    });
    this.depForm = this.fb.group({ name: ['', Validators.required] });
    this.desForm = this.fb.group({ name: ['', Validators.required] });
    this.annForm = this.fb.group({ title: ['', Validators.required], content: ['', Validators.required] });

    // Manager is required only when creating an EMPLOYEE.
    const applyManagerValidation = (role: string) => {
      const managerControl = this.empForm.get('managerId');
      if (!managerControl) return;

      if (role === 'EMPLOYEE') {
        managerControl.setValidators([Validators.required]);
      } else {
        managerControl.clearValidators();
        managerControl.setValue('');
      }
      managerControl.updateValueAndValidity({ emitEvent: false });
    };

    this.empForm.get('role')?.valueChanges.subscribe(role => applyManagerValidation((role || '').toString()));
    applyManagerValidation((this.empForm.get('role')?.value || '').toString());
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.get<any[]>('/users').subscribe(r => this.users = r);
    this.api.get<any[]>('/employees').subscribe({
      next: r => {
        this.employees = r || [];
        this.managers = this.employees
          .filter(e => e?.roleId === 2 && (e?.status || '').toString().toUpperCase() === 'ACTIVE')
          .sort((a, b) => ((a?.fullName || '') as string).localeCompare((b?.fullName || '') as string));
      },
      error: () => {
        this.employees = [];
        this.managers = [];
      }
    });
    this.api.get<any>('/leaves/team').subscribe({
      next: r => this.teamLeaves = r?.leaves || [],
      error: () => this.teamLeaves = []
    });
    this.api.get<any[]>('/admin/departments').subscribe(r => this.departments = r);
    this.api.get<any[]>('/admin/designations').subscribe(r => this.designations = r);
    this.api.get<any[]>('/admin/announcements').subscribe(r => this.announcements = r);
  }

  saveEmployee(): void {
    this.empSubmitted = true;
    this.createEmpError = '';
    if (this.empForm.invalid) {
      this.empForm.markAllAsTouched();
      this.createEmpError = 'Please correct validation errors before creating employee.';
      return;
    }

    const employeeId = (this.empForm.value.employeeId || '').toString().trim();
    const email = (this.empForm.value.email || '').toString().trim().toLowerCase();
    const role = (this.empForm.value.role || '').toString().trim();
    const rawManagerId = (this.empForm.value.managerId || '').toString().trim();
    const managerId = rawManagerId ? Number(rawManagerId) : null;

    if (role === 'EMPLOYEE') {
      if (!managerId) {
        this.createEmpError = 'Please select a manager for the employee.';
        return;
      }
      const validManager = this.managers.some(m => Number(m?.id) === managerId);
      if (!validManager) {
        this.createEmpError = 'Selected manager is invalid. Please reload and try again.';
        return;
      }
    }

    const duplicateEmployeeId = this.users.some(u => (u.employeeId || '').toString().trim() === employeeId);
    if (duplicateEmployeeId) {
      this.createEmpError = 'Employee ID already exists.';
      return;
    }

    const duplicateEmail = this.users.some(u => (u.email || '').toString().trim().toLowerCase() === email);
    if (duplicateEmail) {
      this.createEmpError = 'Email already exists.';
      return;
    }

    if (!this.departments[0]?.id || !this.designations[0]?.id) {
      this.createEmpError = 'Create at least one department and designation before adding employees.';
      return;
    }

    const userPayload = {
      employeeId,
      fullName: this.empForm.value.fullName,
      email,
      password: this.empForm.value.password,
      role
    };

    this.api.post<any>('/users', userPayload).subscribe({
      next: createdUser => {
        const defaultDepartmentId = this.departments[0].id;
        const defaultDesignationId = this.designations[0].id;

        const employeePayload = {
          userId: createdUser.id,
          email,
          fullName: this.empForm.value.fullName,
          phone: null,
          address: null,
          emergencyContact: null,
          departmentId: defaultDepartmentId,
          designationId: defaultDesignationId,
          joiningDate: new Date().toISOString().slice(0, 10),
          salary: 1,
          managerId: role === 'EMPLOYEE' ? managerId : null,
          roleId: this.mapRoleToId(role)
        };

        this.api.post('/employees', employeePayload).subscribe({
          next: () => {
            this.createEmpError = '';
            this.empSubmitted = false;
            this.empForm.reset({ role: 'EMPLOYEE', managerId: '' });
            this.load();
          },
          error: err => {
            this.createEmpError = err?.error?.error || 'User was created, but employee profile creation failed.';
            this.load();
          }
        });
      },
      error: err => {
        const backendError = err?.error;
        if (backendError?.error) {
          this.createEmpError = backendError.error;
        } else if (backendError && typeof backendError === 'object') {
          const firstMessage = Object.values(backendError)[0] as string;
          this.createEmpError = firstMessage || 'Unable to create employee.';
        } else {
          this.createEmpError = 'Unable to create employee.';
        }
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.empForm.get(controlName);
    return !!(control && control.invalid && (this.empSubmitted || control.touched || control.dirty));
  }

  setActive(id: number, active: boolean): void {
    this.api.patch(`/users/${id}/active?active=${active}`, {}).subscribe(() => this.load());
  }

  mapRoleToId(role: string): number {
    switch (role) {
      case 'ADMIN':
        return 3;
      case 'MANAGER':
        return 2;
      default:
        return 1;
    }
  }

  saveDepartment(): void {
    this.depError = '';
    if (this.depForm.invalid) {
      this.depForm.markAllAsTouched();
      this.depError = 'Department name is required.';
      return;
    }
    const normalizedName = (this.depForm.value.name || '').toString().trim();
    const name = normalizedName.toLowerCase();
    const exists = this.departments.some(d => (d.name || '').toString().trim().toLowerCase() === name);
    if (exists) {
      this.depError = 'Department already exists.';
      return;
    }
    this.savingDepartment = true;
    this.api.post('/admin/departments', { name: normalizedName }).subscribe({
      next: () => {
        this.depError = '';
        this.depForm.reset();
        this.savingDepartment = false;
        this.load();
      },
      error: err => {
        this.depError = err?.error?.error || err?.error?.message || 'Unable to save department.';
        this.savingDepartment = false;
      }
    });
  }

  saveDesignation(): void {
    this.desError = '';
    if (this.desForm.invalid) {
      this.desForm.markAllAsTouched();
      this.desError = 'Designation name is required.';
      return;
    }
    const normalizedName = (this.desForm.value.name || '').toString().trim();
    const name = normalizedName.toLowerCase();
    const exists = this.designations.some(d => (d.name || '').toString().trim().toLowerCase() === name);
    if (exists) {
      this.desError = 'Designation already exists.';
      return;
    }
    this.savingDesignation = true;
    this.api.post('/admin/designations', { name: normalizedName }).subscribe({
      next: () => {
        this.desError = '';
        this.desForm.reset();
        this.savingDesignation = false;
        this.load();
      },
      error: err => {
        this.desError = err?.error?.error || err?.error?.message || 'Unable to save designation.';
        this.savingDesignation = false;
      }
    });
  }

  saveAnnouncement(): void {
    if (this.annForm.invalid) return;
    this.api.post('/admin/announcements', this.annForm.value).subscribe(() => { this.annForm.reset(); this.load(); });
  }

  approveLeave(id: number): void {
    this.leaveError = '';
    const comment = (this.leaveComments[id] || '').trim();
    this.api.patch(`/leaves/${id}/approve`, { comment }).subscribe({
      next: () => this.load(),
      error: err => this.leaveError = err?.error?.error || 'Unable to approve leave.'
    });
  }

  rejectLeave(id: number): void {
    this.leaveError = '';
    const comment = (this.leaveComments[id] || '').trim();
    if (!comment) {
      this.leaveError = 'Reject comment is required.';
      return;
    }
    this.api.patch(`/leaves/${id}/reject`, { comment }).subscribe({
      next: () => this.load(),
      error: err => this.leaveError = err?.error?.error || 'Unable to reject leave.'
    });
  }
}
