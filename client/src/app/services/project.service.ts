import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Pageable, PaginatedRequest, Sort } from './model/request.paginated';
import { BaseDocument } from './model/response.document';
import { Diagram } from './diagram.service';



// Responses
export interface ErrorResponse {
  error: string;
}

export interface Project {
  id: string,
  name: string
  userName: string
  diagrams: Diagram[]
  createdAt: any
  updatedAt: any
}


export interface ProjectsPaginatedResponse {
  content: Project[]
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





@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly baseUrl = '/api/project';

  private readonly recentProjectsKey = 'recent_projects';

  constructor(private http: HttpClient) {}

  
  
  

  getProject(id: string): Observable<Project> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });

    return this.http.get<Project>(`${this.baseUrl}/${id}`, { headers }).pipe(
      tap((project) => this.saveToRecentProjects(project))
    );
  }

  getProjects(request: PaginatedRequest): Observable<ProjectsPaginatedResponse> {
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
    return this.http.get<ProjectsPaginatedResponse>(
      `${this.baseUrl}`,
      { headers, params }
    );
  }

  getRecentProjects(): Project[] {
    const stored = localStorage.getItem(this.recentProjectsKey);
    return stored ? JSON.parse(stored) : [];
  }

  deleteProject(id: string): Observable<string> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true'
    });
  
    return this.http.delete(`${this.baseUrl}/${id}`, {
      headers,
      observe: 'body',
      responseType: 'text'
    }).pipe(
      tap(() => this.removeFromRecentProjects(id))
    );
  }

  updateProject(project: Project): Observable<BaseDocument> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true'
    });
  
    return this.http.put<BaseDocument>(`${this.baseUrl}/${project.id}`, project, { headers }).pipe(
      tap(() => this.saveToRecentProjects(project))
    );
  }

  createProject(project: Project): Observable<BaseDocument> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true' // tells interceptor to include bearer token
    });
    return this.http.post<BaseDocument>(`${this.baseUrl}`, project, {headers});
  }

  private saveToRecentProjects(project: Project): void {
    const stored = localStorage.getItem(this.recentProjectsKey);
    let recentProjects: any[] = stored ? JSON.parse(stored) : [];
  
    // Rimuovi se già presente (per evitare duplicati)
    recentProjects = recentProjects.filter(p => p.id !== project.id);
  
    const minimalProject = { id: project.id, name: project.name };
    // Aggiungi in cima
    recentProjects.unshift(minimalProject);
  
    // Limita a max 5 (o quanto vuoi)
    if (recentProjects.length > 5) {
      recentProjects = recentProjects.slice(0, 5);
    }
    
    localStorage.setItem(this.recentProjectsKey, JSON.stringify(recentProjects));
  }

  private removeFromRecentProjects(projectId: string): void {
    const stored = localStorage.getItem(this.recentProjectsKey);
    let recentProjects: any[] = stored ? JSON.parse(stored) : [];
  
    recentProjects = recentProjects.filter(p => p.id !== projectId);
    localStorage.setItem(this.recentProjectsKey, JSON.stringify(recentProjects));
  }
 
}
