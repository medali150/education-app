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
import { ProfessorService } from '../../../core/services/professor.service';
import { Professor } from '../../../core/models/professor.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-professor-list',
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
  templateUrl: './professor-list.component.html',
  styleUrls: ['./professor-list.component.scss']
})
export class ProfessorListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'email', 'department', 'actions'];
  dataSource = new MatTableDataSource<Professor>();
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private professorService: ProfessorService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadProfessors();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadProfessors(): void {
    this.isLoading = true;
    this.professorService.getProfessors().subscribe({
      next: (response) => {
        // Map professor_id to id for frontend compatibility
        const professorsWithIds = response.professors.map((professor, index) => {
          // If professor has professor_id but no id, map it
          if (professor.professor_id !== undefined && professor.id === undefined) {
            return { ...professor, id: professor.professor_id };
          }
          // If professor has neither, create a temporary ID
          else if (professor.id === undefined && professor.professor_id === undefined) {
            return { ...professor, id: -(index + 1) };
          }
          return professor;
        });
        
        this.dataSource.data = professorsWithIds;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Error loading professors. ' + (error.message || ''), 'Close', {
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
    this.router.navigate(['/professors/add']);
  }

  editProfessor(professor: Professor): void {
    // Check if this is a temporary ID (negative number)
    if (professor.id !== undefined && professor.id < 0) {
      this.isLoading = true;
      
      // First save the professor to get a real ID
      this.professorService.createProfessor({
        name: professor.name,
        email: professor.email,
        department: professor.department,
        password: 'defaultPassword123'
      }).subscribe({
        next: (createdProfessor) => {
          this.isLoading = false;
          // Now navigate to edit with the real ID
          this.router.navigate([`/professors/edit/${createdProfessor.id}`]);
          // Refresh the list to show the new ID
          this.loadProfessors();
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Error creating professor. ' + (error.message || ''), 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
      return;
    }
    
    // For professors with real IDs, proceed with navigation
    this.router.navigate([`/professors/edit/${professor.id}`]);
  }

  deleteProfessor(professor: Professor): void {
    // First check if the professor has an ID
    if (professor.id === undefined || professor.id === null) {
      this.snackBar.open('Cannot delete professor: ID is missing', 'Close', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      return;
    }
  
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Delete',
        message: `Are you sure you want to delete professor ${professor.name}?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.isLoading = true;
        
        // Check if this is a temporary ID (negative number)
        if (professor.id && professor.id < 0) {
          // For temporary IDs, just remove from the local data source
          // and show success message (since there's no real record in the backend)
          this.dataSource.data = this.dataSource.data.filter(p => p.id !== professor.id);
          this.isLoading = false;
          this.snackBar.open('Professor removed from list', 'Close', {
            duration: 3000
          });
          return;
        }
        
        // For real IDs, proceed with the API call
        this.professorService.deleteProfessor(professor.id as number).subscribe({
          next: () => {
            this.loadProfessors();
            this.snackBar.open('Professor deleted successfully!', 'Close', {
              duration: 3000
            });
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.open('Error deleting professor. ' + (error.message || ''), 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }
}