import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StudentService } from '../../../core/services/student.service';
import { Student } from '../../../core/models/student.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-student-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './student-list.component.html',
  styleUrls: ['./student-list.component.scss']
})
export class StudentListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'email', 'department', 'age', 'actions'];
  dataSource = new MatTableDataSource<Student>();
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private studentService: StudentService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadStudents();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadStudents(): void {
    this.isLoading = true;
    this.studentService.getStudents().subscribe({
      next: (response) => {
        // Map the backend field names to match the frontend model
        this.dataSource.data = response.students.map(student => ({
          id: student.students_id,
          name: student.name,
          email: student.email,
          department: student.department,
          age: student.age,
          password: student.password || ''
        }));
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Error loading students. ' + (error.message || ''), 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  navigateToAdd(): void {
    this.router.navigate(['/students/add']);
  }

  editStudent(student: Student): void {
    if (student.id) {
      this.router.navigate([`/students/edit/${student.id}`]);
    } else {
      this.snackBar.open('Student ID is undefined', 'Close', {
        duration: 3000
      });
    }
  }

  deleteStudent(student: Student): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Delete',
        message: `Are you sure you want to delete student ${student.name}?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && student.id) {
        this.isLoading = true;
        this.studentService.deleteStudent(student.id).subscribe({
          next: () => {
            this.loadStudents();
            this.snackBar.open('Student deleted successfully!', 'Close', {
              duration: 3000
            });
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.open('Error deleting student. ' + (error.message || ''), 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }
}