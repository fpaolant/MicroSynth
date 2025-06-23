import { Component, Input } from '@angular/core';

@Component({
    standalone: true,
    selector: 'app-logo',
    template: `
    <svg class="mx-auto block" [attr.width]="width" [attr.height]="height" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" fill="none">
        <!-- Edges -->
        <line x1="30" y1="50" x2="170" y2="50" stroke="var(--primary-color)" stroke-width="2"/>
        <line x1="30" y1="50" x2="100" y2="150" stroke="var(--primary-color)" stroke-width="2"/>
        <line x1="170" y1="50" x2="100" y2="150" stroke="var(--primary-color)" stroke-width="2"/>
        <line x1="100" y1="150" x2="100" y2="80" stroke="var(--primary-color)" stroke-width="2"/>
        <line x1="30" y1="50" x2="100" y2="80" stroke="var(--primary-color)" stroke-width="2"/>

        <!-- Nodes -->
        <circle cx="30" cy="50" r="8" fill="var(--primary-color)"/>
        <circle cx="170" cy="50" r="8" fill="var(--primary-color)"/>
        <circle cx="100" cy="150" r="8" fill="var(--primary-color)"/>
        <circle cx="100" cy="80" r="8" fill="var(--primary-color)"/>
    </svg>
    `
})
export class AppLogo {

    @Input() width: string = '100%';
    @Input() height: string = '100%';

}