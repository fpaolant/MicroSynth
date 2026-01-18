import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FaviconService } from './app/layout/service/favicon.service';
import { ToastModule } from 'primeng/toast';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterModule, ToastModule],
    template: `
        <router-outlet></router-outlet>
        <p-toast appendTo="body" position="bottom-right" key="br" />
    `
})
export class AppComponent {
    constructor(private faviconService: FaviconService) {}
}
