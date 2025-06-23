import { Routes } from '@angular/router';
import { AccountPage } from './account/account.component';
import { authGuard } from '../services/auth.guard';
import { AppLayout } from '../layout/component/app.layout';
import { ProjectsPage } from './projects/projects.component';
import { ProjectPage } from './project/project.component';
import { DiagramPage } from './diagram/diagram.component';

export default [
    { path: 'account', component: AccountPage, canActivate: [authGuard] },
    { path: 'projects', component: ProjectsPage, canActivate: [authGuard] },
    { path: 'project/:id', component: ProjectPage, canActivate: [authGuard] },
    { path: 'diagram', component: DiagramPage, canActivate: [authGuard] },
    { path: 'diagram/:id', component: DiagramPage, canActivate: [authGuard] },

    //{ path: 'admin', component: AdminPage, canActivate: [authGuard], data: { role: 'ADMIN' } },  
    { path: 'admin',
        loadChildren: () => import('./admin/admin.routes'), 
        canActivate: [authGuard], data: { role: 'ADMIN' }
    },  
    { path:  'auth',
        component: AppLayout,
        loadChildren: () => import('./auth/auth.routes') 
    },
    { path: '**', redirectTo: '/notfound' }
] as Routes;
