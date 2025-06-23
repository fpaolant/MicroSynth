import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

// Requests
export interface UserCredentials {
  username: string;
  password: string;
}

// Responses
export interface TokenResponse {
  token: string;
}

export interface UserRequest {
  username: string;
  password: string;
  firstname: string;
  lastname: string;
  email: string;
}



@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'auth_token';
  private readonly baseUrl = '/api/auth';

  constructor(private http: HttpClient) {}

  
  /**
   * Effettua il login dell'utente e restituisce il token JWT.
   * @param credentials Credenziali dell'utente (username e password).
   * @returns Un Observable contenente il TokenResponse o un errore.
   */
  login(credentials: UserCredentials): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap((response) => this.saveToken(response.token))
    );
  }

  logout(): void {
    this.removeToken();
  }

  register(user: UserRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/register`, user);
  }

  isLogged(): boolean {
    return !!this.getToken();
  }

  getRoles(): string | null {
    const decodedToken = this.decodeToken();
    return decodedToken?.roles || null;
  }

  isAdmin(): boolean {
    const roles = this.getRoles();
    return roles ? roles.includes('ADMIN') : false;
  }

  getUsername(): string | null {
    const decodedToken = this.decodeToken();
    return decodedToken?.sub || null;
  }

  getUserId(): string | null {
    const decodedToken = this.decodeToken();
    return decodedToken?.userId || null;
  }

  // Metodi per la gestione del token JWT
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  removeToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  /**
   * Verifica la validità di un token JWT.
   * @param token Il token JWT da verificare.
   * @returns Un Observable che emette `true` se il token è valido, altrimenti `false`.
   */
  checkTokenValidity(token: TokenResponse): Observable<boolean> {
    return this.http.post<boolean>(`${this.baseUrl}/check-token`, token);
  }

  isTokenValid(): Observable<boolean> {
    const token = this.getToken();
    
    if (token) {
      return this.checkTokenValidity({ token }).pipe(
        tap((isValid) => {
          if (!isValid) {
            this.removeToken();
          }
        })
      );     
    } else {
      return new Observable<boolean>((observer) => {
        observer.next(false);
        observer.complete();
      });
    }
  }

  decodeToken(): any | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      return jwtDecode(token);
    } catch (error) {
      console.error('Error decoding JWT token', error);
      return null;
    }
  }
}
