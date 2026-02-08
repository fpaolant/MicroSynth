import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { LayoutService } from '../service/layout.service';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: `
    <ul class="layout-menu">
        <ng-container *ngFor="let item of model; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul> 

    

    <ul class="layout-menu">
        <ng-container *ngFor="let item of modelUser; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul> 

    <ul class="layout-menu" *ngIf="recentProjects.length > 0">
        <ng-container *ngFor="let item of recentProjects; let i = index">
             <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul>

    <ul class="layout-menu">
        <ng-container *ngFor="let item of modelAdmin; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul> 

    `
})
export class AppMenu implements OnInit {
    model: MenuItem[] = [];
    modelAdmin: MenuItem[] = [];
    modelUser: MenuItem[] = [];
    recentProjects: MenuItem[] = [];

    authService = inject(AuthService);
    projectService = inject(ProjectService);
    layoutService = inject(LayoutService);
    
    private destroyRef: DestroyRef = inject(DestroyRef);
    

    ngOnInit() {
        this.model = [
            {
                items: [
                    { label: 'Home', icon: 'pi pi-fw pi-home', routerLink: ['/'] },
                ]
            },
        ];
        if(this.authService.isAdmin()){
            this.modelAdmin = [
                {
                    label: 'Admin',
                    items: [
                        { label: 'Accounts', icon: 'pi pi-fw pi-users', routerLink: ['/pages/admin/accounts'] }
                    ]
                }
            ];
        }

        if(this.authService.isLogged()){
            this.layoutService.menuProjects$
            .pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
                console.log("update received")
                this.loadRecentProjects();
            });
            this.modelUser = [
                {
                    label: '',
                    items: [
                        { label: 'My projects', icon: 'pi pi-fw pi-images', routerLink: ['/pages/projects'] }
                    ]
                },
            ];
            this.loadRecentProjects();
        }

    }

    private loadRecentProjects() {
        // Carica progetti recenti da localStorage
        const recent = this.projectService.getRecentProjects();
        if(recent.length > 0) {
            this.recentProjects = [
                {
                label: 'Recent projects',
                items: recent.map((project) => ({
                    label: project.name,
                    icon: 'pi pi-fw pi-clock',
                    routerLink: [`/pages/project/${project.id}`]
                }))
                }
            ];
        }    
    }


}
