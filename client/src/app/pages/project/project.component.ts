import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { Project, ProjectService } from '../../services/project.service';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { AppLogo } from '../../layout/component/app.logo';

@Component({
  selector: 'app-project',
  imports: [CommonModule, RouterModule, BreadcrumbModule, DataViewModule, ButtonModule, AppLogo],
  providers: [ConfirmationService, MessageService],
  standalone: true,
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
}) export class ProjectPage implements OnInit {
  routerService = inject(Router);
  projectService = inject(ProjectService);
  confirmationService = inject(ConfirmationService);
  messageService = inject(MessageService);

  projectId: string | null = null;

  project: Project | null = null;

  items = signal<MenuItem[]>([]);

  constructor(private route: ActivatedRoute) {
    this.items.set([{ icon: 'pi pi-home', route: '/' }, { label: 'Projects', route: '/pages/projects' }]);
  }


  ngOnInit() {
    this.projectId = this.route.snapshot.paramMap.get('id')!;

    this.route.params.subscribe(params => {
      this.projectId = params['id'];
      this.loadProject();  // ricarica il progetto anche se è lo stesso id
    });
  }

  loadProject() { 
    if (!this.projectId) {
      console.error('Project ID is null');
      return;
    }
    // Carica il progetto usando projectId
    this.projectService.getProject(this.projectId).subscribe({
      next: (project) => {
        // Aggiungi il progetto alla lista
        this.items.update((items) => [
          ...items,
          { label: project.name }
        ]);
        // Imposta il progetto caricato
        this.project = project;
      },
      error: (error) => {
        console.error('Error loading project:', error);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load project' });
      }
    });

  }

  newDiagram() {
    this.routerService.navigate(['/pages/diagram'], { state: { project: this.project } });
  }

  selectDiagram(diagramId: string) {
    this.routerService.navigate(['/pages/diagram', diagramId], { state: { project: this.project } });
  }

  deleteDiagram(diagramId: string) {
    console.log('Deleting diagram:', diagramId);
  }
}