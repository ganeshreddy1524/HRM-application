import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UiStateService {
  private loginPanelSubject = new BehaviorSubject<boolean>(false);
  loginPanel$ = this.loginPanelSubject.asObservable();

  openLoginPanel(): void {
    this.loginPanelSubject.next(true);
  }

  closeLoginPanel(): void {
    this.loginPanelSubject.next(false);
  }
}
