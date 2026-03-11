import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './components/dashboard.component';
import { LeaveComponent } from './components/leave.component';
import { PerformanceComponent } from './components/performance.component';
import { GoalsComponent } from './components/goals.component';
import { AdminComponent } from './components/admin.component';
import { DirectoryComponent } from './components/directory.component';
import { LandingComponent } from './components/landing.component';
import {  AuthInterceptor } from './services/auth.interceptor';


@NgModule({
  declarations: [
    AppComponent,
    LandingComponent,
    DashboardComponent,
    LeaveComponent,
    PerformanceComponent,
    GoalsComponent,
    AdminComponent,
    DirectoryComponent
  ],
  imports: [BrowserModule, FormsModule, ReactiveFormsModule, HttpClientModule, AppRoutingModule],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
