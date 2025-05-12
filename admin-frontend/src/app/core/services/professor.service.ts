import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError, tap, map } from 'rxjs';
import { Professor } from '../models/professor.model';

@Injectable({ providedIn: 'root' })
export class ProfessorService {
  private apiUrl = 'http://localhost:8000/admin/professors';
  
  constructor(private http: HttpClient) {}

  getProfessors(): Observable<{ professors: Professor[] }> {
    console.log('Fetching professors from:', this.apiUrl);
    return this.http.get<{ professors: Professor[] }>(this.apiUrl)
      .pipe(
        tap(response => console.log('Professors response:', response)),
        catchError(error => {
          console.error('Error fetching professors:', error);
          return this.handleError(error);
        })
      );
  }

  getProfessor(id: number): Observable<Professor> {
    if (!id) {
      return throwError(() => new Error('Professor ID is required'));
    }
    return this.http.get<Professor>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(response => console.log('Get professor response:', response)),
        catchError(this.handleError)
      );
  }

  createProfessor(data: Partial<Professor>): Observable<Professor> {
    console.log('Creating professor with data:', data);
    return this.http.post<Professor>(this.apiUrl, data)
      .pipe(
        map((response: any) => {
          // Map professor_id to id if needed
          if (response.professor_id && !response.id) {
            return { ...response, id: response.professor_id };
          }
          return response;
        }),
        tap((response: Professor) => console.log('Create professor response:', response)),
        catchError(error => {
          console.error('Error creating professor:', error);
          return this.handleError(error);
        })
      );
  }

  updateProfessor(id: number, data: Partial<Professor>): Observable<Professor> {
    if (!id) {
      return throwError(() => new Error('Professor ID is required'));
    }
    const url = `${this.apiUrl}/${id}`;
    console.log('Sending PUT request to:', url, 'with data:', data);
    return this.http.put<Professor>(url, data)
      .pipe(
        map((response: any) => {
          console.log('Raw update response:', response);
          // Map professor_id to id if needed
          if (response.professor_id && !response.id) {
            return { ...response, id: response.professor_id };
          }
          return response;
        }),
        tap((response: Professor) => console.log('Update professor response:', response)),
        catchError(error => {
          console.error('Error updating professor:', error);
          return this.handleError(error);
        })
      );
  }

  deleteProfessor(id: number): Observable<any> {
    if (!id) {
      return throwError(() => new Error('Professor ID is required'));
    }
    return this.http.delete(`${this.apiUrl}/${id}`)
      .pipe(
        tap(response => console.log('Delete professor response:', response)),
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
