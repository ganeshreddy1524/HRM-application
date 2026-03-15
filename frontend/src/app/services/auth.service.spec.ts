import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('login persists accessToken as token', () => {
    service.login({ username: 'a@b.com', password: 'secret' }).subscribe(res => {
      expect(res.role).toBe('ADMIN');
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'a@b.com', password: 'secret' });

    req.flush({
      accessToken: 'access-123',
      refreshToken: 'refresh-123',
      userId: 7,
      role: 'ADMIN',
      fullName: 'Test User',
      email: 'a@b.com',
    });

    expect(localStorage.getItem('token')).toBe('access-123');
    expect(localStorage.getItem('userId')).toBe('7');
    expect(localStorage.getItem('role')).toBe('ADMIN');
    expect(localStorage.getItem('fullName')).toBe('Test User');
    expect(localStorage.getItem('email')).toBe('a@b.com');
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('login supports legacy token field', () => {
    service.login({ username: 'x', password: 'y' }).subscribe();

    const req = httpMock.expectOne(environment.apiBaseUrl + '/auth/login');
    req.flush({
      token: 'legacy-1',
      role: 'EMPLOYEE',
      fullName: 'Legacy',
    });

    expect(localStorage.getItem('token')).toBe('legacy-1');
    expect(localStorage.getItem('role')).toBe('EMPLOYEE');
    expect(localStorage.getItem('fullName')).toBe('Legacy');
  });

  it('persistAuth throws when no token present', () => {
    expect(() =>
      service.persistAuth({
        role: 'EMPLOYEE',
        fullName: 'No Token',
      } as any),
    ).toThrowError(/did not include a token/i);
  });

  it('resetPassword posts to reset endpoint', () => {
    service.resetPassword({ employeeId: '100', email: 'x@y.com', newPassword: 'new' }).subscribe();

    const req = httpMock.expectOne(environment.apiBaseUrl + '/auth/reset-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ employeeId: '100', email: 'x@y.com', newPassword: 'new' });
    req.flush(null);
  });

  it('logout clears storage and navigates home', () => {
    localStorage.setItem('token', 't');
    const navigateSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});

