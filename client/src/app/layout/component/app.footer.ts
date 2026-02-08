import { Component } from '@angular/core';

@Component({
    standalone: true,
    selector: 'app-footer',
    template: `<div class="layout-footer">
        MicroSynth {{ year }} 
    </div>`
})
export class AppFooter {
    year = new Date().getFullYear();
}
