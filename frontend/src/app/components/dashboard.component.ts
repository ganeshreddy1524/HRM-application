import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  template: `
  <div class="row g-3">
    <div class="col-md-4"><div class="card p-3"><h6>Welcome</h6><h4>{{ me?.fullName }}</h4><small>{{ me?.role }}</small></div></div>
    <div class="col-md-4"><div class="card p-3"><h6>Notifications</h6><h4>{{ notifications.length }}</h4></div></div>
    <div class="col-md-4"><div class="card p-3"><h6>Unread Notifications</h6><h4>{{ unreadNotifications }}</h4></div></div>
  </div>

  <div class="row g-3 mt-1">
    <div class="col-lg-6">
      <div class="card p-3">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Personal Information</h5>
          <button class="btn btn-sm btn-outline-primary" *ngIf="!isEditingProfile && canEditProfile" (click)="startEdit()">Edit</button>
        </div>

        <div *ngIf="!isEditingProfile">
          <div><strong>Email:</strong> {{ me?.email || '-' }}</div>
          <div><strong>Phone:</strong> {{ me?.phone || '-' }}</div>
          <div><strong>Address:</strong> {{ me?.address || '-' }}</div>
          <div><strong>Emergency Contact:</strong> {{ me?.emergencyContact || '-' }}</div>
        </div>

        <form class="rwf-form" *ngIf="isEditingProfile" [formGroup]="profileForm" (ngSubmit)="saveProfile()">
          <div class="mb-2">
            <label class="form-label">Phone</label>
            <input class="form-control" formControlName="phone">
          </div>
          <div class="mb-2">
            <label class="form-label">Address</label>
            <textarea class="form-control" formControlName="address"></textarea>
          </div>
          <div class="mb-2">
            <label class="form-label">Emergency Contact</label>
            <input class="form-control" formControlName="emergencyContact">
          </div>
          <div class="text-success small mb-2" *ngIf="profileSaved">Profile updated successfully.</div>
          <div class="d-flex gap-2">
            <button class="btn btn-primary" type="submit">Save Details</button>
            <button class="btn btn-outline-secondary" type="button" (click)="cancelEdit()">Cancel</button>
          </div>
        </form>
      </div>

      <div class="card p-3 mt-3">
        <h5>Send Notification</h5>
        <form class="rwf-form" [formGroup]="notifyForm" (ngSubmit)="sendNotification()">
          <div class="form-check mb-2" *ngIf="me?.role === 'ADMIN'">
            <input class="form-check-input" type="checkbox" id="sendAll" formControlName="sendToAll">
            <label class="form-check-label" for="sendAll">Send to all users</label>
          </div>

          <div class="mb-2" *ngIf="!notifyForm.value.sendToAll">
            <label class="form-label">Recipient</label>
            <select class="form-select" formControlName="recipientUserId">
              <option value="">Select recipient</option>
              <option *ngFor="let u of recipients" [value]="u.id">{{ u.fullName }} ({{ u.employeeId }})</option>
            </select>
          </div>

          <div class="mb-2">
            <label class="form-label">Message</label>
            <textarea class="form-control" formControlName="message"></textarea>
          </div>

          <div class="text-success small mb-2" *ngIf="notifySuccess">Notification sent.</div>
          <div class="text-danger small mb-2" *ngIf="notifyError">{{ notifyError }}</div>
          <button class="btn btn-primary" [disabled]="notifyForm.invalid">Send</button>
        </form>
      </div>
    </div>

    <div class="col-lg-6">
      <div class="card p-3 mb-3" *ngIf="me?.role === 'MANAGER' || me?.role === 'ADMIN'">
        <h5>Team Leave Approvals</h5>
        <div *ngIf="teamLeaves.length === 0" class="text-muted">No team leave requests.</div>
        <div *ngFor="let l of teamLeaves" class="border rounded p-2 mb-2">
          <div><strong>{{ l.employeeName || '-' }}</strong></div>
          <div>{{ l.leaveType }}: {{ l.startDate }} to {{ l.endDate }}</div>
          <div>Reason: {{ l.reason }}</div>
          <div class="mb-2">Status: {{ l.status }}</div>
          <textarea *ngIf="l.status === 'PENDING'"
                    class="form-control mb-2" rows="2"
                    [ngModel]="decisionComments[l.id] || ''"
                    (ngModelChange)="decisionComments[l.id] = $event"
                    [ngModelOptions]="{standalone: true}"
                    placeholder="Comment (required for reject)"></textarea>
          <div class="d-flex gap-2" *ngIf="l.status === 'PENDING'">
            <button class="btn btn-sm btn-success" (click)="approveLeave(l.id)">Approve</button>
            <button class="btn btn-sm btn-outline-danger" (click)="rejectLeave(l.id)">Reject</button>
          </div>
        </div>
      </div>

      <div class="card p-3">
        <h5>Recent Notifications</h5>
        <div *ngIf="notifications.length === 0" class="text-muted">No notifications yet.</div>

        <div *ngFor="let n of notifications" class="border rounded p-2 mb-2">
          <div class="d-flex justify-content-between align-items-center">
            <span>{{ n.message }}</span>
            <button class="btn btn-sm" [ngClass]="n.readFlag ? 'btn-outline-secondary' : 'btn-outline-success'"
                    (click)="toggleRead(n)">
              {{ n.readFlag ? 'Read' : 'Mark Read' }}
            </button>
          </div>
          <small class="text-muted">Status: {{ n.readFlag ? 'Read' : 'Unread' }}</small>
        </div>
      </div>
    </div>
  </div>
  `
})
export class DashboardComponent implements OnInit {
  me: any;
  notifications: any[] = [];
  unreadNotifications = 0;
  recipients: any[] = [];
  teamLeaves: any[] = [];
  decisionComments: { [key: number]: string } = {};
  canEditProfile = false;

  isEditingProfile = false;
  profileSaved = false;
  notifySuccess = false;
  notifyError = '';

  profileForm: FormGroup;
  notifyForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder, private auth: AuthService) {
    this.profileForm = this.fb.group({
      phone: [''],
      address: [''],
      emergencyContact: ['']
    });

    this.notifyForm = this.fb.group({
      sendToAll: [false],
      recipientUserId: [''],
      message: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadDashboard();
    this.loadRecipients();
  }

  loadDashboard(): void {
    this.api.get<any>('/users/me').pipe(
      catchError(() => {
        this.auth.logout();
        return of(null);
      })
    ).subscribe(authUser => {
      if (!authUser) {
        return;
      }
      this.api.get<any>('/employees/profile').pipe(
        catchError(() => of(null))
      ).subscribe(profile => {
        this.canEditProfile = !!profile;
        this.me = { ...authUser, ...profile };
        this.profileForm.patchValue({
          phone: this.me?.phone || '',
          address: this.me?.address || '',
          emergencyContact: this.me?.emergencyContact || ''
        });
      });

      if (authUser.role === 'MANAGER' || authUser.role === 'ADMIN') {
        this.api.get<any>('/leaves/team').subscribe({
          next: response => this.teamLeaves = response?.leaves || [],
          error: () => this.teamLeaves = []
        });
      } else {
        this.teamLeaves = [];
      }
    });

    this.api.get<any[]>('/admin/notifications').subscribe(r => {
      this.notifications = r;
      this.unreadNotifications = r.filter(x => !x.readFlag).length;
    });
  }

  loadRecipients(): void {
    this.api.get<any[]>('/users/search?q=').subscribe(r => this.recipients = r);
  }

  startEdit(): void {
    if (!this.canEditProfile) {
      return;
    }
    this.isEditingProfile = true;
    this.profileSaved = false;
  }

  cancelEdit(): void {
    this.isEditingProfile = false;
    this.profileSaved = false;
    this.profileForm.patchValue({
      phone: this.me?.phone || '',
      address: this.me?.address || '',
      emergencyContact: this.me?.emergencyContact || ''
    });
  }

  saveProfile(): void {
    if (!this.canEditProfile) {
      return;
    }

    this.profileSaved = false;
    this.api.put('/employees/profile', this.profileForm.value).subscribe(() => {
      this.profileSaved = true;
      this.isEditingProfile = false;
      this.loadDashboard();
    });
  }

  sendNotification(): void {
    this.notifySuccess = false;
    this.notifyError = '';

    const payload = {
      message: (this.notifyForm.value.message || '').toString().trim(),
      sendToAll: !!this.notifyForm.value.sendToAll,
      recipientUserId: this.notifyForm.value.sendToAll ? null : Number(this.notifyForm.value.recipientUserId)
    };

    if (!payload.sendToAll && !payload.recipientUserId) {
      this.notifyError = 'Please select a recipient.';
      return;
    }

    const request = payload.sendToAll
      ? this.api.post('/admin/notifications/broadcast', { message: payload.message })
      : this.api.post('/admin/notifications/send', payload);

    request.subscribe({
      next: () => {
        this.notifySuccess = true;
        this.notifyForm.patchValue({ message: '', recipientUserId: '', sendToAll: false });
      },
      error: err => this.notifyError = err?.error?.error || 'Unable to send notification.'
    });
  }

  toggleRead(notification: any): void {
    if (notification.readFlag) {
      return;
    }

    this.api.patch(`/admin/notifications/${notification.id}/read`, {}).subscribe(() => {
      notification.readFlag = true;
      this.unreadNotifications = this.notifications.filter(x => !x.readFlag).length;
    });
  }

  approveLeave(id: number): void {
    const comment = (this.decisionComments[id] || '').trim();
    this.api.patch(`/leaves/${id}/approve`, { comment }).subscribe(() => {
      delete this.decisionComments[id];
      this.loadDashboard();
    });
  }

  rejectLeave(id: number): void {
    const comment = (this.decisionComments[id] || '').trim();
    if (!comment) {
      this.notifyError = 'Reject comment is required.';
      return;
    }
    this.api.patch(`/leaves/${id}/reject`, { comment }).subscribe(() => {
      delete this.decisionComments[id];
      this.loadDashboard();
    });
  }
}
