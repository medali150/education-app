import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, throwError, tap } from 'rxjs';
import { Student } from '../models/student.model';

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class StudentService {
  private apiUrl = 'http://localhost:8000/admin/students';
  
  constructor(private http: HttpClient) {}

  // Add console logs to track API calls
  getStudents(): Observable<{ students: Student[] }> {
    console.log('Fetching students from:', this.apiUrl);
    return this.http.get<{ students: Student[] }>(this.apiUrl)
      .pipe(
        tap(response => console.log('Students response:', response)),
        catchError(error => {
          console.error('Error fetching students:', error);
          return this.handleError(error);
        })
      );
  }

  getStudent(id: number | undefined): Observable<Student> {
    if (!id) {
      return throwError(() => new Error('Student ID is required'));
    }
    return this.http.get<Student>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createStudent(data: Partial<Student>): Observable<Student> {
    console.log('Creating student with data:', data);
    return this.http.post<Student>(this.apiUrl, data)
      .pipe(
        tap(response => console.log('Create student response:', response)),
        catchError(error => {
          console.error('Error creating student:', error);
          return this.handleError(error);
        })
      );
  }

  updateStudent(id: number | undefined, data: Partial<Student>): Observable<Student> {
    if (!id) {
      return throwError(() => new Error('Student ID is required'));
    }
    return this.http.put<Student>(`${this.apiUrl}/${id}`, data)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteStudent(id: number | undefined): Observable<any> {
    if (!id) {
      return throwError(() => new Error('Student ID is required'));
    }
    return this.http.delete(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }
  
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error?.message || `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    
    console.error('API Error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
