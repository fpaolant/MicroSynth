import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { BaseDocument } from './model/response.document';



// Responses
export interface ErrorResponse {
  error: string;
}

export interface Diagram {
  id: string | null
  name: string,
  data: DiagramData | null,
}

export interface DiagramData {
  nodes: DiagramNode[];
  connections: DiagramConnection[];
}

export interface DiagramConnection {
  id: string;
  source: string;
  target: string;
  isLoop: boolean;
  weight: number;
  label: string;
  payload: {
    code: string;
    language: string;
  };
}

export interface DiagramNode {
  id: string;
  label: string;
  shape: string;
  payload: {
    code: string;
    language: string;
  };
  weight: number;
}






@Injectable({
  providedIn: 'root',
})
export class DiagramService {
  private readonly baseUrl = '/api/diagram';

  constructor(private http: HttpClient) {}

  getEmptyDiagram(): Diagram {
    return {
      id: null,
      name: "New Graph",
      data: null
    }
  }

  
  updateDiagram(projectId: string, diagram: Diagram): Observable<BaseDocument> {
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      'Use-Auth': 'true'
    });

    return this.http.put<BaseDocument>(`${this.baseUrl}/${projectId}`, diagram, {headers});
  }
 
}
