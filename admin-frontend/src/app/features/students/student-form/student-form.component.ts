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
import { MatSnackBar } from '@angular/material/snack-bar';
import { StudentService } from '../../../core/services/student.service';
import { Student } from '../../../core/models/student.model';

@Component({
  selector: 'app-student-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './student-form.component.html',
  styleUrls: ['./student-form.component.scss']
})
export class StudentFormComponent implements OnInit {
  studentForm!: FormGroup;
  isLoading = false;
  isEditMode = false;
  studentId: number | null = null;
  currentYear = new Date().getFullYear();

  constructor(
    private fb: FormBuilder,
    private studentService: StudentService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    
    // Check if we're in edit mode
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.studentId = +params['id'];
        this.loadStudentData(this.studentId);
      }
    });
  }

  initForm(): void {
    this.studentForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      department: ['', Validators.required],
      age: ['', [Validators.required, Validators.min(16), Validators.max(100)]],
      password: ['defaultPassword123', Validators.required] // Set default password
    });
  }

  loadStudentData(id: number): void {
    this.isLoading = true;
    this.studentService.getStudent(id).subscribe({
      next: (student) => {
        // Map backend fields to frontend model
        this.studentForm.patchValue({
          name: student.name,
          email: student.email,
          department: student.department,
          age: student.age
        });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Error loading student data. ' + (error.message || ''), 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.studentForm.valid) {
      this.isLoading = true;
      const studentData = {
        name: this.studentForm.value.name,
        email: this.studentForm.value.email,
        department: this.studentForm.value.department,
        age: this.studentForm.value.age,
        password: this.studentForm.value.password || 'defaultPassword123'
      };

      if (this.isEditMode && this.studentId) {
        this.studentService.updateStudent(this.studentId, studentData).subscribe({
          next: () => {
            this.isLoading = false;
            this.snackBar.open('Student updated successfully!', 'Close', { duration: 3000 });
            this.router.navigate(['/students']);
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.open('Error updating student. ' + (error.message || ''), 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
      } else {
        this.studentService.createStudent(studentData).subscribe({
          next: () => {
            this.isLoading = false;
            this.snackBar.open('Student created successfully!', 'Close', { duration: 3000 });
            this.router.navigate(['/students']);
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.open('Error creating student. ' + (error.message || ''), 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/students']);
  }
}