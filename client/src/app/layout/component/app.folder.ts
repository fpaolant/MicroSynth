import { Component, Input } from '@angular/core';

@Component({
    standalone: true,
    selector: 'app-folder',
    template: `
            <svg class="mx-auto block"
     [attr.width]="width"
     [attr.height]="height"
     viewBox="0 0 200 200"
     xmlns="http://www.w3.org/2000/svg"
     fill="none">

  <!-- Binder outer -->
  <rect x="30" y="35"
        width="140" height="130"
        rx="10" ry="10"
        stroke="var(--primary-color)"
        stroke-width="2"
        fill="none"/>

  <!-- Binder spine (left strip) -->
  <rect x="30" y="35"
        width="26" height="130"
        rx="10" ry="10"
        stroke="var(--primary-color)"
        stroke-width="2"
        fill="none"/>

  <!-- Ring holes -->
  <circle cx="43" cy="70" r="6"
          stroke="var(--primary-color)" stroke-width="2" fill="none"/>
  <circle cx="43" cy="100" r="6"
          stroke="var(--primary-color)" stroke-width="2" fill="none"/>
  <circle cx="43" cy="130" r="6"
          stroke="var(--primary-color)" stroke-width="2" fill="none"/>

  <!-- ===== Graph A (top-left) ===== -->
  <line x1="70" y1="70" x2="98" y2="70" stroke="var(--primary-color)" stroke-width="2"/>
  <line x1="70" y1="70" x2="84" y2="92" stroke="var(--primary-color)" stroke-width="2"/>
  <line x1="98" y1="70" x2="84" y2="92" stroke="var(--primary-color)" stroke-width="2"/>

  <circle cx="70" cy="70" r="6" fill="var(--primary-color)"/>
  <circle cx="98" cy="70" r="6" fill="var(--primary-color)"/>
  <circle cx="84" cy="92" r="6" fill="var(--primary-color)"/>

  <!-- ===== Graph B (top-right) ===== -->
  <line x1="118" y1="75" x2="148" y2="92" stroke="var(--primary-color)" stroke-width="2"/>
  <line x1="148" y1="92" x2="118" y2="110" stroke="var(--primary-color)" stroke-width="2"/>

  <circle cx="118" cy="75" r="6" fill="var(--primary-color)"/>
  <circle cx="148" cy="92" r="6" fill="var(--primary-color)"/>
  <circle cx="118" cy="110" r="6" fill="var(--primary-color)"/>

  <!-- ===== Graph C (bottom) ===== -->
  <line x1="92" y1="132" x2="130" y2="132" stroke="var(--primary-color)" stroke-width="2"/>
  <line x1="92" y1="132" x2="110" y2="150" stroke="var(--primary-color)" stroke-width="2"/>

  <circle cx="92"  cy="132" r="6" fill="var(--primary-color)"/>
  <circle cx="130" cy="132" r="6" fill="var(--primary-color)"/>
  <circle cx="110" cy="150" r="6" fill="var(--primary-color)"/>

</svg>

    `
})
export class AppFolder {

    @Input() width: string = '100%';
    @Input() height: string = '100%';

}