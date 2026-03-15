import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { environment } from '../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('GET uses apiBaseUrl', () => {
    service.get<{ ok: boolean }>('/health').subscribe(res => {
      expect(res.ok).toBeTrue();
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/health');
    expect(req.request.method).toBe('GET');
    req.flush({ ok: true });
  });

  it('POST uses apiBaseUrl', () => {
    service.post<{ id: number }>('/items', { name: 'x' }).subscribe(res => {
      expect(res.id).toBe(123);
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/items');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'x' });
    req.flush({ id: 123 });
  });

  it('PUT uses apiBaseUrl', () => {
    service.put<{ ok: boolean }>('/items/1', { name: 'y' }).subscribe(res => {
      expect(res.ok).toBeTrue();
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/items/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ name: 'y' });
    req.flush({ ok: true });
  });

  it('PATCH uses apiBaseUrl', () => {
    service.patch<{ ok: boolean }>('/items/1', { name: 'z' }).subscribe(res => {
      expect(res.ok).toBeTrue();
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/items/1');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ name: 'z' });
    req.flush({ ok: true });
  });

  it('DELETE uses apiBaseUrl', () => {
    service.delete<{ ok: boolean }>('/items/1').subscribe(res => {
      expect(res.ok).toBeTrue();
    });

    const req = httpMock.expectOne(environment.apiBaseUrl + '/items/1');
    expect(req.request.method).toBe('DELETE');
    req.flush({ ok: true });
  });
});

