import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Pageable, PaginatedRequest, Sort } from './model/request.paginated';

// Requests
export interface OpenAccountRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  surname: string;
  mobile: string;
}

// Responses
export interface ErrorResponse {
  error: string;
}

export function getPossibleRoles(): string[] {
  return ['USER', 'ADMIN'];
}



export interface AccountsPaginatedResponse {
  content: Account[]
  pageable: Pageable
  last: boolean
  totalPages: number
  totalElements: number
  first: boolean
  size: number
  number: number
  sort: Sort
  numberOfElements: number
  empty: boolean
}

export interface Account {
  id: string
  username: string
  firstname: string
  lastname: string
  email: string
  roles: string[]
  createdAt: string
  updatedAt: string
}





@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly tokenKey = 'auth_token';
  private readonly baseUrl = '/api/account';

  constructor(private http: HttpClient) {}

  
  openAccountUser(accountRequest: OpenAccountRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/user-account`, accountRequest).pipe(
      tap({
        next: (response: any) => {
          console.log('User account created successfully', response);
        },
        error: (error: any) => {
          console.error('Error creating user account', error);
        }
      })
    );
  }
  

  getAccount(id: string): Observable<Account> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });

    return this.http.get<Account>(`${this.baseUrl}/${id}`, { headers });
  }

  getAccounts(request: PaginatedRequest): Observable<AccountsPaginatedResponse> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    const params = {
      page: request.page.toString(),
      size: request.size.toString(),
      sortBy: request.sortBy,
      sortDir: request.sortDir
    };
    return this.http.get<AccountsPaginatedResponse>(
      `${this.baseUrl}/all`,
      { headers, params }
    );
  }

  promoteAccount(id: string): Observable<string> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.put(
      `${this.baseUrl}/${id}/promote`,
      null,
      {
        headers: headers,
        observe: 'body',
        responseType: 'text'
      }
    );
  }

  demoteAccount(id: string): Observable<string> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.put(
      `${this.baseUrl}/${id}/demote`,
      null,
      {
        headers: headers,
        observe: 'body',
        responseType: 'text'
      }
    );
  }

  deleteAccount(id: string): Observable<string> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.delete(
      `${this.baseUrl}/${id}`,
      {
        headers: headers,
        observe: 'body',
        responseType: 'text'
      }
    );
  }

  updateAccount(account: Account): Observable<any> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.put<void>(`${this.baseUrl}/${account.id}`, account, {headers});
  }

  createAccount(account: Account): Observable<any> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.post<void>(`${this.baseUrl}/create`, account, {headers});
  }

 
}
