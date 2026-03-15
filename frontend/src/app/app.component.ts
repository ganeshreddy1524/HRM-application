import { Component } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { UiStateService } from './services/ui-state.service';

@Component({
  selector: 'app-root',
  template: `
  <nav class="navbar navbar-expand-lg navbar-dark rwf-navbar">
    <div class="container">
      <a class="navbar-brand d-flex align-items-center gap-2" [routerLink]="auth.isLoggedIn() ? '/dashboard' : '/'">
        <img src="assets/rwf-logo.svg" alt="RevWorkforce" class="rwf-logo">
        <span>RevWorkforce</span>
      </a>

      <ng-container *ngIf="auth.isLoggedIn(); else guestHeaderActions">
        <button class="navbar-toggler"
                type="button"
                aria-controls="rwfNavbar"
                aria-label="Toggle navigation"
                [attr.aria-expanded]="menuOpen"
                (click)="toggleMenu()">
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="rwfNavbar" [class.show]="menuOpen">
          <div class="navbar-nav ms-auto">
            <a class="nav-link" routerLink="/dashboard" (click)="closeMenu()">Dashboard</a>
            <a class="nav-link" routerLink="/leaves" (click)="closeMenu()">Leaves</a>
            <a class="nav-link" routerLink="/performance" (click)="closeMenu()">Performance</a>
            <a class="nav-link" routerLink="/goals" (click)="closeMenu()">Goals</a>
            <a class="nav-link" *ngIf="auth.role() !== 'EMPLOYEE'" routerLink="/directory" (click)="closeMenu()">Directory</a>
            <a class="nav-link" *ngIf="auth.role() === 'ADMIN'" routerLink="/admin" (click)="closeMenu()">Admin</a>
            <a class="nav-link" href="#" (click)="logout($event)">Logout</a>
          </div>
        </div>
      </ng-container>

      <ng-template #guestHeaderActions>
        <div class="d-flex gap-2 ms-auto">
          <button class="btn btn-light btn-sm rwf-header-btn" (click)="openHeaderLogin()">Login</button>
        </div>
      </ng-template>
    </div>
  </nav>

  <div class="scanner-wrap" *ngIf="loading">
    <div class="scanner-line"></div>
  </div>

  <main class="container py-4 page-shell">
    <router-outlet></router-outlet>
  </main>
  `
})
export class AppComponent {
  loading = false;
  menuOpen = false;

  constructor(public auth: AuthService, private router: Router, private uiState: UiStateService) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.loading = true;
      }
      if (event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
        this.menuOpen = false;
        setTimeout(() => this.loading = false, 180);
      }
    });
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  openHeaderLogin(): void {
    this.router.navigate(['/']).then(() => this.uiState.openLoginPanel());
  }

  logout(event: Event): void {
    event.preventDefault();
    this.menuOpen = false;
    this.auth.logout();
  }
}
