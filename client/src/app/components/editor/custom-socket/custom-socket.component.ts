import {
    Component,
    Input,
    HostBinding,
    ChangeDetectorRef,
    OnChanges,
    OnInit
  } from "@angular/core";
  
  
  @Component({
    standalone: true,
    imports: [],
    selector: "app-custom-socket",
    template: ``,
    styleUrls: ["./custom-socket.component.scss"]
  })
  export class CustomSocketComponent implements OnChanges, OnInit {
    @Input() data!: any;
    @Input() rendered!: any;
  
    @HostBinding("title") get title() {
      return this.data.name;
    }
  
    constructor(private cdr: ChangeDetectorRef) {
      this.cdr.detach();
    }
    
    ngOnInit(): void {
        console.log("custom socket component data ",this.data, "rendered ", this.rendered);
    }
  
    ngOnChanges(): void {
      this.cdr.detectChanges();
      requestAnimationFrame(() => this.rendered());
    }
  }