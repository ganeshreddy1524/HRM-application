import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { throwError, of } from 'rxjs';
import { LandingComponent } from './landing.component';
import { AuthService } from '../services/auth.service';
import { UiStateService } from '../services/ui-state.service';

describe('LandingComponent', () => {
  let fixture: ComponentFixture<LandingComponent>;
  let component: LandingComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let uiState: UiStateService;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'resetPassword', 'logout', 'isLoggedIn', 'role', 'token']);

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterTestingModule],
      declarations: [LandingComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        UiStateService,
      ],
    });

    fixture = TestBed.createComponent(LandingComponent);
    component = fixture.componentInstance;
    uiState = TestBed.inject(UiStateService);
    fixture.detectChanges();
  });

  it('validates username as employeeId or email', () => {
    component.form.patchValue({ username: 'bad@' });
    component.form.controls.username.markAsTouched();
    component.form.controls.username.updateValueAndValidity();
    expect(component.form.controls.username.errors?.['employeeIdOrEmail']).toBeTrue();

    component.form.patchValue({ username: '1001' });
    component.form.controls.username.updateValueAndValidity();
    expect(component.form.controls.username.errors).toBeNull();

    component.form.patchValue({ username: 'a@b.com' });
    component.form.controls.username.updateValueAndValidity();
    expect(component.form.controls.username.errors).toBeNull();
  });

  it('shows backend message instead of generic error', () => {
    authService.login.and.returnValue(
      throwError(() => ({
        status: 401,
        error: { error: 'Authentication Error', message: 'Invalid email or password' },
      })),
    );

    component.form.patchValue({ username: 'a@b.com', password: 'wrong' });
    component.submit();

    expect(component.error).toBe('Invalid email or password');
  });

  it('shows server unreachable error when status is 0', () => {
    authService.login.and.returnValue(
      throwError(() => ({ status: 0, error: null })),
    );
    component.form.patchValue({ username: '1001', password: 'x' });
    component.submit();
    expect(component.error).toContain('Unable to reach server');
  });

  it('uses backend error when message is missing', () => {
    authService.login.and.returnValue(
      throwError(() => ({ status: 401, error: { error: 'Invalid email or password' } })),
    );
    component.form.patchValue({ username: '1001', password: 'x' });
    component.submit();
    expect(component.error).toBe('Invalid email or password');
  });

  it('falls back to default message when backend has no details', () => {
    authService.login.and.returnValue(
      throwError(() => ({ status: 500, error: null })),
    );
    component.form.patchValue({ username: '1001', password: 'x' });
    component.submit();
    expect(component.error).toBe('Invalid email or password.');
  });

  it('navigates to dashboard on success', () => {
    authService.login.and.returnValue(of({ accessToken: 't', refreshToken: 'r', role: 'ADMIN', fullName: 'U' } as any));
    const router = TestBed.inject(Router);
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.form.patchValue({ username: '1001', password: 'ok' });
    component.submit();

    expect(navSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('resets state when login panel closes', () => {
    component.error = 'x';
    component.resetMode = true;
    component.resetError = 'y';
    component.resetSuccess = 'z';

    uiState.openLoginPanel();
    uiState.closeLoginPanel();

    expect(component.showLoginPanel).toBeFalse();
    expect(component.error).toBe('');
    expect(component.resetMode).toBeFalse();
    expect(component.resetError).toBe('');
    expect(component.resetSuccess).toBe('');
  });

  it('submitReset validates confirm password', () => {
    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Different123',
    });

    component.submitReset();
    expect(component.resetError).toContain('must match');
  });

  it('submitReset shows backendError.error when present', () => {
    authService.resetPassword.and.returnValue(
      throwError(() => ({ status: 400, error: { error: 'User not found' } })),
    );

    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Password123',
    });

    component.submitReset();
    expect(component.resetError).toBe('User not found');
  });

  it('submitReset falls back to first error field in object responses', () => {
    authService.resetPassword.and.returnValue(
      throwError(() => ({ status: 400, error: { email: 'Invalid email format' } })),
    );

    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Password123',
    });

    component.submitReset();
    expect(component.resetError).toBe('Invalid email format');
  });

  it('submitReset shows generic message for unknown backend shapes', () => {
    authService.resetPassword.and.returnValue(
      throwError(() => ({ status: 400, error: 'bad' })),
    );

    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Password123',
    });

    component.submitReset();
    expect(component.resetError).toBe('Unable to reset password.');
  });

  it('submitReset shows generic message when object has no usable values', () => {
    authService.resetPassword.and.returnValue(
      throwError(() => ({ status: 400, error: {} })),
    );

    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Password123',
    });

    component.submitReset();
    expect(component.resetError).toBe('Unable to reset password.');
  });

  it('submitReset sets success message on success', () => {
    authService.resetPassword.and.returnValue(of(void 0));

    component.openResetMode();
    component.resetForm.patchValue({
      employeeId: '1001',
      email: 'a@b.com',
      newPassword: 'Password123',
      confirmPassword: 'Password123',
    });

    component.submitReset();
    expect(component.resetSuccess).toContain('Password reset successful');
  });
});
