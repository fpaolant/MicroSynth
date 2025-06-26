import {
  Component,
  ElementRef,
  ViewChild,
  Injector,
  OnInit,
  inject,
  AfterViewInit,
  Output,
  EventEmitter,
  Input,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';


import { getUID, NodeEditor } from "rete";
import { AngularPlugin, Presets as AngularPresets } from "rete-angular-plugin/19";
import { AreaPlugin, AreaExtensions, Area2D } from "rete-area-plugin";
import {
  ConnectionPlugin,
} from "rete-connection-plugin";

import {
  AutoArrangePlugin,
  Presets as ArrangePresets,
  ArrangeAppliers
} from "rete-auto-arrange-plugin";


import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { SplitButtonModule } from 'primeng/splitbutton';
// customizations
import { CustomSocketComponent } from "./custom-socket/custom-socket.component";
import { CustomConnectionComponent } from './custom-connection/custom-connection.component';
import { CustomNodeComponent } from './custom-node/custom-node.component';

import { ContextMenuPlugin, Presets as ContextMenuPresets } from "rete-context-menu-plugin";
import { UniPortConnector } from './util/uniport-connector';
import { Connection, Node, Socket } from './presets';
import { ComputedSocketPosition } from './util/util';
import { AreaExtra, Schemes, Shape } from './types';
import { DropdownChangeEvent, DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { MinimapPlugin } from 'rete-minimap-plugin';
import { ConnectionPathPlugin, Transformers } from "rete-connection-path-plugin";
import { curveStep, curveMonotoneX, curveLinear, CurveFactory, curveNatural,  } from "d3-shape";
import { pathTransformer, useTransformerUpdater } from './util/path';
import { hasCycle } from './util/math';
import { DiagramData } from '../../services/diagram.service';
import { TooltipModule } from 'primeng/tooltip';
import { UploadFileDialogComponent } from '../upload-file-dialog/upload-file-dialog.component';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { forkJoin, from, Observable, of } from 'rxjs';
import { finalize, switchMap, take } from 'rxjs/operators';
import { ConfirmDialogModule } from 'primeng/confirmdialog';









@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, ToolbarModule, ButtonModule, IconFieldModule, InputIconModule, 
    SplitButtonModule, DropdownModule, TooltipModule, UploadFileDialogComponent, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './editor.component.html',
  styles: [
    `
      .editor-container {
        width: 100%;
        height: calc(100vh - 24rem);
      }
    `,
  ],
})
export class EditorComponent implements OnInit, AfterViewInit {
  @ViewChild('editor', { static: true }) containerRef!: ElementRef;
  injector = inject(Injector);
  messageService = inject(MessageService);
  confirmationService = inject(ConfirmationService);

  private _diagram!: DiagramData;
  @Input()
  set diagram(value: DiagramData) {
    this._diagram = value;
    if (this.editor) {
      this.loadDiagram(false);
    }
  }
  
  get diagram(): DiagramData {
    return this._diagram;
  }

  @Input() title:string|undefined = ''; 

  @Output() editorEventsChange = new EventEmitter<any>();
  @Output() areaEventsChange = new EventEmitter<any>();

  @Output() nodeAdded = new EventEmitter<Node>();
  @Output() nodeSelected = new EventEmitter<Node>();
  @Output() nodeRemoved = new EventEmitter<Node>();
  @Output() nodeUpdated = new EventEmitter<Node>();

  @Output() connectionAdded = new EventEmitter<Connection<Node,Node>>();
  @Output() connectionRemoved = new EventEmitter<Connection<Node,Node>>();
  @Output() connectionSelected = new EventEmitter<Connection<Node,Node>>();
  @Output() connectionUpdated = new EventEmitter<Connection<Node,Node>>();

  @Output() editorAreaCleared = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<string>();

  public editor!: NodeEditor<Schemes>;
  private area!: AreaPlugin<Schemes, AreaExtra>;
  private arrange!: AutoArrangePlugin<Schemes>;
  private renderPlugin!: AngularPlugin<Schemes, AreaExtra>;

  private socketPositionWatcher = new ComputedSocketPosition('circle');

  private connectionEvents: any;

  private connectionExcludedFromCycleCheck: Connection<Node, Node>[] = [];

  shapes: Shape[] = ['ellipse', 'circle', 'rect'];
  selectedShape: Shape = 'circle';

  loopConnectionsEnabled = false;
  uploadFileVisible = false;
  diagramTouched = false;
  loadingDiagram = signal(false);
  
  ngOnInit(): void {
      this.editor = new NodeEditor<Schemes>();
  }

  ngAfterViewInit(): void {
      const el = this.containerRef.nativeElement;
  
      if (el) {
        this.buildEditor(el, this.injector);
      }
  }

  private async buildEditor(container: HTMLElement, injector: Injector) {
    this.area = new AreaPlugin<Schemes, AreaExtra>(container);
    const connectionPlugin = new ConnectionPlugin<Schemes, AreaExtra>();
    this.renderPlugin = new AngularPlugin<Schemes, AreaExtra>({ injector });

    const pathPlugin = new ConnectionPathPlugin<Schemes, Area2D<Schemes>>({
      curve: (c) => curveNatural,
      //transformer: () => Transformers.classic({ vertical: false }),
      transformer: (connection) => pathTransformer(this.editor, connection),
      arrow: () => {
        return {
          color: '#34d399'
        };
      }
    });
    
    useTransformerUpdater(this.editor, this.area);
    // add sets of presets to render 
    this.addPresets(this.renderPlugin);

    this.editor.use(this.area);
    this.area.use(connectionPlugin);
    this.area.use(this.renderPlugin);

    // @ts-ignore
    this.renderPlugin.use(pathPlugin);
    this.createMinimapPlugin();

    

    // listener of events in area
    this.area.addPipe((context) => {
      if(context.type === 'nodepicked') this.nodeSelected.emit(this.editor.getNode(context.data.id));
      if(context.type === 'noderemoved') this.nodeRemoved.emit(this.editor.getNode(context.data.id));
      if(context.type === 'nodecreated') this.nodeAdded.emit(this.editor.getNode(context.data.id));

      if(context.type === 'connectioncreated') this.connectionAdded.emit(context.data);
      if(context.type === 'connectionremoved') this.connectionRemoved.emit(context.data);

      if(context.type === 'cleared') this.editorAreaCleared.emit();


      if(context.type === 'cleared') this.connectionExcludedFromCycleCheck =[];

      this.areaEventsChange.emit(context);
      return context;
    });

    // listen on events on the editor
    this.editor.addPipe(async (context) => {
        if (
          ['nodecreated', 'noderemoved', 'nodeupdated', 
          'connectioncreated', 'connectionremoved', 'connectionupdated']
          .includes(context.type)
        ) {
          if(! this.loadingDiagram() ) this.diagramTouched = true;
        }

        if (context.type === "connectionremoved") {
          this.connectionExcludedFromCycleCheck = this.connectionExcludedFromCycleCheck.filter(
            (conn) => !(conn.source === context.data.source && conn.target === context.data.target)
          );
        }

        if (context.type === "connectioncreate") {
          const allowed = await this.canCreateConnection(context.data);
          if (!allowed) return;
        }

        if (context.type === 'noderemoved')  {
          this.editor.getConnections().forEach((con) => {
            if (con.source === context.data.id || con.target === context.data.id) {
              this.editor.removeConnection(con.id);
            }
          });
        }


        this.editorEventsChange.emit(context);
        return context;
    });

    const selector = this.createSelector();
    this.connectionEvents = {
      click: (data: Schemes['Connection']) => {
        selector.selectConnection(data)
      },
      remove: (data: Schemes['Connection']) => {
        this.editor.removeConnection(data.id)
      },
      propertyChange: (key: string, value:any) => {
        this.diagramTouched = true;
      }
    }
    

    connectionPlugin.addPreset(() => new UniPortConnector(this.connectionEvents, this.editor));

    // add arrange
    this.addArrangeFeature();

    // area extensions
    AreaExtensions.simpleNodesOrder(this.area);
    AreaExtensions.zoomAt(this.area, this.editor.getNodes());

    return () => this.area.destroy();
  }

  

  canCreateConnection(data: Connection<Node, Node>): Promise<boolean> {
    let connections = [...this.editor.getConnections(), data].filter((c) => !this.connectionExcludedFromCycleCheck.includes(c));
    
    if (hasCycle(this.editor.getNodes(), connections)) {
      return new Promise((resolve) => {
        this.confirmationService.confirm({
          header: 'Are you sure?',
          message: 'Adding this connection can create cycle inside the graph.',
          acceptLabel: 'Add anyway',
          rejectLabel: 'Cancel',
          accept: () => {
            this.connectionExcludedFromCycleCheck.push(data);
            resolve(true);
          },
          reject: () => resolve(false),
        });
      });
    }
    return Promise.resolve(true);
  }


  // load diagram from component input
  loadDiagram(clearBefore: boolean = false, action: 'init' | 'import' = 'init') {
    if (!this.diagram) return;

    this.loadingDiagram.set(true);
  
    const loadData = (): Observable<boolean[]> => {
      const nodeCalls: Observable<boolean>[] = [];
      const connectionCalls: Observable<boolean>[] = [];
  
      // nodes
      for (let node of this.diagram.nodes) {
        nodeCalls.push(this.addNode(node.id, node.label, node.shape as Shape, node.payload, node.weight));
      }
  
      // connections
      for (let connection of this.diagram.connections) {
        const conn$ = from(this.editor.addConnection({
          id: connection.id,
          source: connection.source,
          sourceOutput: 'default',
          target: connection.target,
          targetInput: 'default',
          isLoop: connection.isLoop,
          weight: connection.weight,
          label: connection.label,
          payload: connection.payload,
          click: this.connectionEvents.click,
          remove: this.connectionEvents.remove,
          propertyChange: this.connectionEvents.propertyChange
        } as Connection<Node, Node>));

        connectionCalls.push(conn$);
      }
  
      
      return forkJoin(nodeCalls).pipe(
        switchMap(() => forkJoin(connectionCalls))
      );
    };
  
    const clear$: Observable<any> = clearBefore
      ? from(this.editor.clear()) // converte Promise in Observable
      : of(null);
  
    clear$.pipe(
      switchMap(() => loadData()),
      take(1),
      finalize(()=>{ this.loadingDiagram.set(false); })
    ).subscribe({
      next: () => {
        this.reorder();
        if(action === 'import') { 
          this.messageService.add({
            severity: 'success',
            summary: 'Graph loaded successfully',
          });
          this.diagramTouched = true;
        }
      },
      error: (err) => {
        console.error("Error during graph import", err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error during graph import',
        });
      }
    });
  }
  

  private addPresets(render: any) {
    const socketPositionWatcher = this.socketPositionWatcher;
    // Add custom presets if needed
    render.addPreset(
      //  Presets.classic.setup()
      AngularPresets.classic.setup({
        socketPositionWatcher,
        customize: {
          node() {
            return CustomNodeComponent;
          },
          connection() {
            return CustomConnectionComponent;
          },
          socket() {
            return CustomSocketComponent;
          },
        },
      })
    );
  }

  private addArrangeFeature() {
    this.arrange = new AutoArrangePlugin<Schemes>();
    const area = this.area;
  
    this.arrange.addPreset(ArrangePresets.classic.setup());
    area.use(this.arrange);
  }

  private createSelector() {
    const selector = AreaExtensions.selector()
    const accumulating = AreaExtensions.accumulateOnCtrl()
    const area = this.area as AreaPlugin<Schemes, AreaExtra>
  
    AreaExtensions.selectableNodes(area, selector, { accumulating });

    
    function unselectConnection(c: Schemes['Connection']) {
      c.selected = false
      area.update('connection', c.id)
    }
  
    function selectConnection(c: Schemes['Connection']) {
      selector.add({
        id: c.id,
        label: 'connection',
        translate() {},
        unselect() {
          unselectConnection(c)
        },
      }, accumulating.active())
      c.selected = true
      area.update('connection', c.id)
    }
  
    return {
      selectConnection,
      unselectConnection
    }
  }

  private createMinimapPlugin() {
    const minimap = new MinimapPlugin<Schemes>({
      boundViewport: true,
      minDistance: 1500,
    });
    this.area.use(minimap);
    this.renderPlugin.addPreset(AngularPresets.minimap.setup({ size: 150}));
  }








  //
  // Methods to interact with the editor
  //
  //
  //
  //
  //
  //
  addNode(
    id: string = getUID(),
    label: string = 'Node A',
    shape: Shape = 'circle',
    payload: any = { code: '', language: 'plaintext' },
    weight: number = 0
  ): Observable<boolean> {
    const node = new Node(label, shape, {
      remove: async (data: Node) => {
        const nodeId = data.id;
        await this.editor.removeNode(data.id);
      },
      duplicate: (data: Node) => {
        this.addNode(getUID(), data.label + ' - Copy', data.shape, data.payload, data.weight);
      },
      propertyChange: (key: string, value:any) => {
        this.diagramTouched = true;
      }
    });
  
    node.id = id;
    node.weight = weight;
    node.payload = payload;
  
    return from(this.editor.addNode(node));
  }
  

  async removeNode(id: string) {
    // Logic to remove a node from the editor
    await this.editor.removeNode(id);
  }

  async clear() {
    // clear the editor
    await this.editor.clear();
  }

  async reorder() {
    const area = this.area;
    const editor = this.editor;
    const applier = new ArrangeAppliers.TransitionApplier<Schemes, never>({
      duration: 500,
      timingFunction: (t) => t,
      async onTick() {
        await AreaExtensions.zoomAt(area, editor.getNodes());
      }
    });
    const options = {
      'elk.algorithm': 'layered',
      'elk.edgeRouting': 'SPLINES',
      'elk.spacing.nodeNode': '180',
      'elk.layered.spacing.nodeNodeBetweenLayers': '180',
    
      'elk.layered.mergeEdges': 'false',
      'elk.spacing.edgeEdge': '40',
      'elk.spacing.edgeEdgeBetweenLayers': '40',
      'elk.layered.crossingMinimization.forceNodeModelOrder': 'true',
    };
    await this.arrange.layout({options, applier});
    // AreaExtensions.zoomAt(this.area, this.editor.getNodes());
  }

  async onShapeChange(event: DropdownChangeEvent) {
    const value = event.value as Shape;

    this.editor.getNodes().forEach(node => {
      node.shape = value
      node.width = 80
      node.height = 80
      node.shape = value
      this.area.update('node', node.id)
    });
  }
  
  exportAsJson() {
    const nodes = this.editor.getNodes();
    const connections = this.editor.getConnections();
    return JSON.stringify({ nodes, connections }, null, 2);
  }

  save() {
    this.onSave.emit(this.exportAsJson())
    this.diagramTouched = false;
  }

  download() {
    const blob = new Blob([this.exportAsJson()], { type: 'application/json' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = this.title? `${this.title}.json`: 'graph.json';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
  }

  onImport() {
    this.uploadFileVisible = true;
  }

  onFileParsed(fileData: { name: string, content: string }) {
    try {
      this.diagram = JSON.parse(fileData.content);
      this.loadDiagram(true, 'import');
      this.messageService.add({
        severity: 'success',
        summary: 'File imported successfully',
        detail: fileData.name,
      });
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: 'File not valid',
        detail: fileData.name,
      });
    }
  }
  
  onCancelUpload() {}

  
}