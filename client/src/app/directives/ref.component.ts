import {
    Directive,
    Input,
    ViewContainerRef,
    ComponentRef,
    OnChanges,
    SimpleChanges,
    OnInit,
  } from '@angular/core';
import { CustomSocketComponent } from '../components/editor/custom-socket/custom-socket.component';
  
  @Directive({
    selector: '[refComponent]',
  })
  export class RefComponentDirective implements OnChanges, OnInit {
    @Input() data: any;
    @Input('data-testid') datatestid: any;
    @Input() emit: any;
  
    private componentRef?: ComponentRef<any>;
  
    constructor(private vcRef: ViewContainerRef) {}

    ngOnInit(): void {
      if (!this.data?.type) return;
  
      this.vcRef.clear();
      let component: any;
  
      if (this.data.type === 'socket') {
        component = CustomSocketComponent;
      }
  
      if (component) {
        this.componentRef = this.vcRef.createComponent(component);
        this.componentRef.instance.data = this.data;
        if ('emit' in this.componentRef.instance) {
          this.componentRef.instance.emit = this.emit;
        }
      }
    }
  
    ngOnChanges(changes: SimpleChanges): void {
      if (this.componentRef && this.data) {
        this.componentRef.instance.data = this.data;
        if ('data-testid' in this.componentRef.instance) {
            this.componentRef.instance['data-testid'] = this.datatestid;
        }
        if ('emit' in this.componentRef.instance) {
          this.componentRef.instance.emit = this.emit;
        }
      }
    }
  
    // chiamata esterna da chi ha accesso alla direttiva per fare il rendering dinamico
    public create(component: any) {
      this.vcRef.clear();
      this.componentRef = this.vcRef.createComponent(component);
      this.componentRef.instance.data = this.data;
      if ('emit' in this.componentRef.instance) {
        this.componentRef.instance.emit = this.emit;
      }
      if ('data-testid' in this.componentRef.instance) {
        this.componentRef.instance['data-testid'] = this.datatestid;
      }
    }   
  }
  