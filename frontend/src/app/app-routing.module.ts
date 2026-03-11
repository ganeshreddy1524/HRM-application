import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard.component';
import { LeaveComponent } from './components/leave.component';
import { PerformanceComponent } from './components/performance.component';
import { GoalsComponent } from './components/goals.component';
import { AdminComponent } from './components/admin.component';
import { DirectoryComponent } from './components/directory.component';
import { LandingComponent } from './components/landing.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'leaves', component: LeaveComponent, canActivate: [AuthGuard] },
  { path: 'performance', component: PerformanceComponent, canActivate: [AuthGuard] },
  { path: 'goals', component: GoalsComponent, canActivate: [AuthGuard] },
  { path: 'directory', component: DirectoryComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['MANAGER', 'ADMIN'] } },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
