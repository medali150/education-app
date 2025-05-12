import { Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login.component';
import { AuthGuard } from './core/guards/auth.guard';
import { StudentListComponent } from './features/students/student-list/student-list.component';
import { StudentFormComponent } from './features/students/student-form/student-form.component';
import { ProfessorListComponent } from './features/professors/professor-list/professor-list.component';
import { ProfessorFormComponent } from './features/professors/professor-form/professor-form.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'students',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: StudentListComponent },
      { path: 'add', component: StudentFormComponent },
      { path: 'edit/:id', component: StudentFormComponent }
    ]
  },
  {
    path: 'professors',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ProfessorListComponent },
      { path: 'add', component: ProfessorFormComponent },
      { path: 'edit/:id', component: ProfessorFormComponent }
    ]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }, // Redirect to login by default
  { path: '**', redirectTo: '/login' } // Redirect unknown paths to login
];
