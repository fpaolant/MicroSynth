import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Diagram } from './diagram.service';




@Injectable({
  providedIn: 'root',
})
export class ExportService {
  private readonly baseUrl = '/api/generator';

  constructor(private http: HttpClient) {}


  exportDockerCompose(projectId: string, diagramId:string): Observable<Blob> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true'
    });

    return this.http.get(`${this.baseUrl}/generate/${projectId}/${diagramId}`, {
      headers,
      responseType: 'blob'
    });
  }
 
}
