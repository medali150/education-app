import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, throwError, of } from 'rxjs';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  admin_id: number;
  name: string;
  email: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8000/admin';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private currentAdminSubject = new BehaviorSubject<LoginResponse | null>(null);
  private readonly TOKEN_KEY = 'auth_token';
  private readonly ADMIN_KEY = 'admin_info';
  private platformId: Object;

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.platformId = platformId;
    this.checkAuthStatus();
  }

  private checkAuthStatus(): void {
    const token = this.getToken();
    console.log('[AuthService] checkAuthStatus token:', token);
    if (token) {
      this.isAuthenticatedSubject.next(true);
      const adminStr = this.getAdminInfo();
      console.log('[AuthService] checkAuthStatus adminStr:', adminStr);
      if (adminStr) {
        try {
          const adminObj = JSON.parse(adminStr);
          console.log('[AuthService] Restored admin object:', adminObj);
          this.currentAdminSubject.next(adminObj);
        } catch (e) {
          console.error('[AuthService] Failed to parse admin info:', e);
          this.currentAdminSubject.next(null);
          this.removeAdminInfo();
        }
      }
    } else {
      this.isAuthenticatedSubject.next(false);
      this.currentAdminSubject.next(null);
    }
  }

  validateToken(token: string): Observable<boolean> {
    return this.http.post<{ valid: boolean }>(`${this.apiUrl}/validate-token`, { token }).pipe(
      map(res => res.valid),
      catchError(() => of(false))
    );
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      try {
        return localStorage.getItem(this.TOKEN_KEY);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  private setToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      try {
        localStorage.setItem(this.TOKEN_KEY, token);
      } catch (e) {}
    }
  }

  private removeToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      try {
        localStorage.removeItem(this.TOKEN_KEY);
      } catch (e) {}
    }
  }

  private setAdminInfo(admin: LoginResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      try {
        localStorage.setItem(this.ADMIN_KEY, JSON.stringify(admin));
      } catch (e) {}
    }
  }

  private removeAdminInfo(): void {
    if (isPlatformBrowser(this.platformId)) {
      try {
        localStorage.removeItem(this.ADMIN_KEY);
      } catch (e) {}
    }
  }

  private getAdminInfo(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      try {
        return localStorage.getItem(this.ADMIN_KEY);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        map(response => {
          if (response.admin_id && response.token) {
            this.setToken(response.token);
            this.setAdminInfo(response);
            this.isAuthenticatedSubject.next(true);
            this.currentAdminSubject.next(response);
            this.router.navigate(['/dashboard']); // Redirect to dashboard after login
          } else {
            throw new Error('Invalid login response');
          }
          return response;
        }),
        catchError(this.handleError)
      );
  }

  logout(): void {
    try {
      this.removeToken();
      this.removeAdminInfo();
      this.isAuthenticatedSubject.next(false);
      this.currentAdminSubject.next(null);
      this.router.navigate(['/login']);
    } catch (e) {
      this.handleError(e);
    }
  }

  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  getCurrentUser(): Observable<LoginResponse | null> {
    return this.currentAdminSubject.asObservable();
  }

  private handleError(error: any) {
    let errorMessage = 'An error occurred';
    if (error instanceof HttpErrorResponse) {
      if (error.error instanceof ErrorEvent) {
        errorMessage = error.error.message;
      } else {
        errorMessage = error.error?.message || error.error?.detail || `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    } else if (error instanceof Error) {
      errorMessage = error.message;
    } else if (typeof error === 'string') {
      errorMessage = error;
    }
    console.error('Auth Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
