import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, throwError } from 'rxjs';
import { Course } from '../models/course';
import { ApiResponse } from './student.service';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = 'http://localhost:8000/admin/courses';

  constructor(private http: HttpClient) {}

  getCourses(): Observable<Course[]> {
    return this.http.get<ApiResponse<Course[]>>(this.apiUrl)
      .pipe(
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  getCourse(id: number): Observable<Course> {
    return this.http.get<ApiResponse<Course>>(`${this.apiUrl}/${id}`)
      .pipe(
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  createCourse(course: Omit<Course, 'courses_id'>): Observable<Course> {
    return this.http.post<ApiResponse<Course>>(this.apiUrl, course)
      .pipe(
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  updateCourse(id: number, course: Partial<Course>): Observable<Course> {
    return this.http.put<ApiResponse<Course>>(`${this.apiUrl}/${id}`, course)
      .pipe(
        map(response => response.data),
        catchError(this.handleError)
      );
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`)
      .pipe(
        map(() => void 0),
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