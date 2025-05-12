import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  adminName: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(admin => {
      if (admin) {
        this.adminName = admin.name;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  navigateToStudents(): void {
    this.router.navigate(['/students']);
  }

  navigateToProfessors(): void {
    this.router.navigate(['/professors']);
  }
}