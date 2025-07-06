import { Component, inject, OnInit, signal } from '@angular/core';
import { DataViewModule } from 'primeng/dataview';
import { Project, ProjectService } from '../../services/project.service';
import { PaginatedRequest } from '../../services/model/request.paginated';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { AppLogo } from '../../layout/component/app.logo';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-projects',
  imports: [RouterModule, CommonModule, ReactiveFormsModule, DataViewModule, DialogModule, ConfirmDialogModule, ButtonModule, InputTextModule, TooltipModule, SkeletonModule, AppLogo],
  providers: [MessageService, ConfirmationService],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.scss'
})
export class ProjectsPage implements OnInit {
  routerService = inject(Router);
  projectService = inject(ProjectService);
  confirmationService = inject(ConfirmationService);
  messageService = inject(MessageService);

  projects = signal<Project[]>([]);

  projectForm: FormGroup;

  loading = signal(false);
  newDialogVisible = {
    value: false,
    set: (val: boolean) => this.newDialogVisible.value = val
  };

  constructor(private fb: FormBuilder) {
    this.projectForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    });
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  private loadProjects() {
    this.loading.set(true);
  
    const request: PaginatedRequest = {
      page: 0,
      size: 100,
      sortBy: 'createdAt',
      sortDir: 'desc'
    };
  
    this.projectService.getProjects(request).subscribe({
      next: (projects) => {
        this.projects.set(projects.content);
      },
      error: (error) => {
        console.error('Error fetching projects:', error);
      },
      complete: () => {
        this.loading.set(false);
      }
    });
  }

  selectProject(id:string) {
    this.routerService.navigate(['/pages/project', id]);
  }

  deleteProject(project: Project) {

    this.confirmationService.confirm({
      message: 'Are you sure you want to delete ' + project.name + '?',
      header: 'Confirm deleting project',
      icon: 'pi pi-exclamation-triangle',
      rejectButtonProps: {
        label: 'Cancel',
        severity: 'secondary',
        outlined: true,
      },
      acceptButtonProps: {
          label: 'Confirm Delete',
          severity: 'danger',
      },
      accept: () => {
        this.projectService.deleteProject(project.id).subscribe({
          next: () => {
            this.loadProjects();
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Project deleted successfully.', life: 3000 });
          },
          error: (error) => {
            console.error('Error deleting project:', error);
          }
        });
      }
    });    
  }

  openProjectDialog() {
    this.newDialogVisible.set(true);
  }

  closeProjectDialog() {
    this.newDialogVisible.set(false);
    this.projectForm.reset();
  }

  saveProject(select: boolean = false) {
    const name = this.projectForm.get('name')?.value;
    this.projectService.createProject({ name } as Project).subscribe({
      next: (doc) => {
        this.projectForm.reset();
        this.newDialogVisible.set(false);
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Project created successfully.', life: 3000 });

        if (select) this.selectProject(doc.id);
        this.loadProjects();
      },
      error: (error) => {
        console.error('Error creating project:', error);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error creating project.', life: 3000 });
      }
    });
  }

  counterArray(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i);
  }

}
