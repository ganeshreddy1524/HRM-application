import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

interface AuthResponse {
  token?: string;
  accessToken?: string;
  refreshToken?: string;
  userId?: number;
  role: string;
  fullName: string;
  email?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private api: ApiService, private router: Router) {}

  login(payload: { username: string; password: string }): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', payload).pipe(
      tap(res => this.persistAuth(res))
    );
  }

  resetPassword(payload: { employeeId: string; email: string; newPassword: string }): Observable<void> {
    return this.api.post<void>('/auth/reset-password', payload);
  }

  persistAuth(res: AuthResponse): void {
    const token = res.token || res.accessToken;
    if (!token) {
      throw new Error('Login response did not include a token.');
    }

    localStorage.setItem('token', token);
    if (res.userId != null) {
      localStorage.setItem('userId', String(res.userId));
    }
    if (res.email) {
      localStorage.setItem('email', res.email);
    }
    localStorage.setItem('role', res.role);
    localStorage.setItem('fullName', res.fullName);
  }

  token(): string | null { return localStorage.getItem('token'); }
  role(): string | null { return localStorage.getItem('role'); }
  isLoggedIn(): boolean { return !!this.token(); }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
