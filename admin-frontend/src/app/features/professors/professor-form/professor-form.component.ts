import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { ProfessorService } from '../../../core/services/professor.service';
import { Professor } from '../../../core/models/professor.model';

@Component({
  selector: 'app-professor-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './professor-form.component.html',
  styleUrls: ['./professor-form.component.scss']
})
export class ProfessorFormComponent implements OnInit {
  professorForm!: FormGroup;
  isLoading = false;
  isEditMode = false;
  professorId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private professorService: ProfessorService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.professorId = +params['id'];
        this.loadProfessorData(this.professorId);
      }
    });
  }

  initForm(): void {
    this.professorForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      department: ['', Validators.required],
      password: ['defaultPassword123', Validators.required]
    });
  }

  loadProfessorData(id: number): void {
    this.isLoading = true;
    this.professorService.getProfessor(id).subscribe({
      next: (professor) => {
        this.professorForm.patchValue({
          name: professor.name,
          email: professor.email,
          department: professor.department
        });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Error loading professor data. ' + (error.message || ''), 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.professorForm.valid) {
      this.isLoading = true;
      const professorData = {
        ...this.professorForm.value,
        password: 'defaultPassword123'
      };
  
      if (this.isEditMode && this.professorId) {
        // Before sending the update request, check if the ID is valid
        if (this.professorId && this.professorId > 0) {
          // Only proceed with update if ID is positive (real database ID)
          this.professorService.updateProfessor(this.professorId, professorData).subscribe({
            next: () => {
              this.isLoading = false;
              this.snackBar.open('Professor updated successfully!', 'Close', { duration: 3000 });
              this.router.navigate(['/professors']);
            },
            error: (error) => {
              this.isLoading = false;
              this.snackBar.open('Error updating professor. ' + (error.message || ''), 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar']
              });
            }
          });
        } else {
          // Show error message for temporary IDs
          this.isLoading = false;
          this.snackBar.open('Cannot update professor with temporary ID', 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      } else {
        this.professorService.createProfessor(professorData).subscribe({
          next: () => {
            this.isLoading = false;
            this.snackBar.open('Professor created successfully!', 'Close', { duration: 3000 });
            this.router.navigate(['/professors']);
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.open('Error creating professor. ' + (error.message || ''), 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/professors']);
  }
}
