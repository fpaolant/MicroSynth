import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FaviconService } from './app/services/favicon.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterModule],
    template: `<router-outlet></router-outlet>`
})
export class AppComponent {
    constructor(private faviconService: FaviconService) {}
}
