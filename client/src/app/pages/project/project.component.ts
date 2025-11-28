import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { Project, ProjectService } from '../../services/project.service';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { AppLogo } from '../../layout/component/app.logo';
import { Diagram, DiagramService } from '../../services/diagram.service';
import { MessagesModule } from 'primeng/messages';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { InputGroupModule } from 'primeng/inputgroup';

@Component({
  selector: 'app-project',
  imports: [CommonModule, RouterModule, BreadcrumbModule, DataViewModule, ButtonModule, AppLogo, MessagesModule, ConfirmDialogModule, InputGroupAddonModule, InputGroupModule],
  providers: [ConfirmationService, MessageService],
  standalone: true,
  templateUrl: './project.component.html',
  styleUrl: './project.component.scss'
}) export class ProjectPage implements OnInit {
  routerService = inject(Router);
  projectService = inject(ProjectService);
  diagramService = inject(DiagramService);
  confirmationService = inject(ConfirmationService);
  messageService = inject(MessageService);


  projectId: string | null = null;

  project = signal<Project | null>(null);

  items = signal<MenuItem[]>([]);

  // flags
  changeName:boolean = false;
    
  // drafts
  nameDraft:string = '';

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
        this.project.set(project);
        this.nameDraft = project.name
      },
      error: (error) => {
        console.error('Error loading project:', error);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load project' });
      }
    });

  }

  newDiagram() {
    const projectId = this.project()?.id;
    this.routerService.navigate(['/pages/diagram',projectId,'new']);
  }

  selectDiagram(diagramId: string) {
    //this.routerService.navigate(['/pages/diagram', diagramId], { state: { project: this.project() } });
    const projectId = this.project()?.id;
    this.routerService.navigate(['/pages/diagram', projectId, diagramId]);
  }

  deleteDiagram(diagram: Diagram) {
    if (!diagram.id) return;
  
    this.confirmationService.confirm({
      message: `Are you sure you want to delete the diagram "${diagram.name}"?`,
      header: 'Confirm',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.diagramService.deleteDiagram(this.project()!.id, diagram.id!).subscribe({
        next: () => {
          this.messageService.add({ key: 'br', severity: 'success', summary: 'Deleted', detail: 'Diagram deleted' });
          this.loadProject();
        },
        error: (err) => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Delete failed: ' + err })
      })
    });
  }


  onNameChange($event: string) {
    const currentProject = this.project();
    if(currentProject === null) return;
    const oldName = currentProject.name;
    currentProject.name = $event;

    this.projectService.updateProject(currentProject).subscribe({
      next: (updatedProject) => {
        // update breadcrumb
        this.project.set(currentProject);

        this.updateBreadcrumb($event)
        this.changeName = false;
      },
      error: (error) => {
        console.error("Error updating name:", error);
        this.project()!.name = oldName;
        this.messageService.add({
          severity: "error",
          summary: "Error",
          detail: "Failed to save name",
        });
      },
    });

    
  }

  onCancelName() {
    this.nameDraft = this.project!.name;
    this.changeName = false;
  }

  private updateBreadcrumb(name: string) {
    // update breadcrumb
    this.items.update((items: MenuItem[]) => {
      const last = items.pop();
      if (last) {
        last.label = name;
        return [
          ...items,
          last
        ];
      }
      return items;
    });
  }



}