import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Diagram } from './diagram.service';




@Injectable({
  providedIn: 'root',
})
export class ExportService {
  private readonly baseUrl = '/api/diagram';

  constructor(private http: HttpClient) {}


  exportDockerCompose(diagram: Diagram): Observable<Blob> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true'
    });
    return this.http.post(`${this.baseUrl}/export/compose`, diagram, {
      headers,
      responseType: 'blob'
    });
  }
 
}
