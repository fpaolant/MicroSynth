import { CommonModule } from "@angular/common";
import {
  AfterViewInit,
  Component,
  inject,
  OnInit,
  signal,
  ViewChild,
} from "@angular/core";
import { RouterModule } from "@angular/router";
import { ActivatedRoute, Router } from "@angular/router";
import { MenuItem, MessageService } from "primeng/api";
import { BreadcrumbModule } from "primeng/breadcrumb";
import { Project, ProjectService } from "../../services/project.service";
import { ButtonModule } from "primeng/button";
import { SplitterModule } from "primeng/splitter";
import { EditorComponent } from "../../components/editor/editor.component";
import {
  Diagram,
  DiagramConnection,
  DiagramData,
  DiagramNode,
  DiagramService,
} from "../../services/diagram.service";
import { InputGroupAddon } from "primeng/inputgroupaddon";
import { InputGroupModule } from "primeng/inputgroup";
import { ExportService } from "../../services/export.service";
import { LayoutService } from "../../layout/service/layout.service";

@Component({
  selector: "app-diagram",
  imports: [
    CommonModule,
    RouterModule,
    BreadcrumbModule,
    ButtonModule,
    EditorComponent,
    SplitterModule,
    InputGroupAddon,
    InputGroupModule
  ],
  providers: [MessageService],
  templateUrl: "./diagram.component.html",
  styleUrl: "./diagram.component.scss",
})
export class DiagramPage implements OnInit, AfterViewInit {
  @ViewChild("editor") editor!: EditorComponent;

  // services
  projectService = inject(ProjectService);
  diagramService = inject(DiagramService);
  exportService = inject(ExportService);
  messageService = inject(MessageService);
  layoutService = inject(LayoutService);

  // breadcrumbs
  items = signal<MenuItem[]>([]);

  // objects
  project: Project | null = null;
  diagram: Diagram | null = null;

  // flags
  diagramTouched: boolean = false;
  changeName:boolean = false;
  
  // drafts
  nameDraft:string = '';

  showDrawer: boolean = false;


  diagramData: {diagram: DiagramData, mode: "init" | "import" | "generate"};
  


  constructor(private router: Router, private route: ActivatedRoute) {
    this.items.set([
      { icon: "pi pi-home", route: "/" },
      { label: "Projects", route: "/pages/projects" },
    ]);

    this.diagramData = {diagram: { nodes: [], connections: [] }, mode: "init"};
  }
  
  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    if (projectId) {
      this.loadProject(projectId);
    } else {
      console.error("Project ID is null");
      this.messageService.add({
        severity: "error",
        summary: "Error",
        detail: "Cannot loading project",
      });
    }
  }
  
  ngAfterViewInit(): void {
    this.layoutService.onMenuToggle()
  }

  loadProject(id: string) {
    this.projectService.getProject(id).subscribe({
      next: (project) => {
        this.project = project;
        // Recupera l'id del diagramma (se presente nei parametri)
        const diagramId = this.route.snapshot.paramMap.get('diagramId');

        // find diagram in project or create an empty one
        this.diagram =
          project.diagrams.find((d) => d.id === diagramId) ||
          this.diagramService.getEmptyDiagram();

        // update breadcrumbs
        this.items.update((items) => [
          ...items,
          { label: project.name, route: `/pages/project/${project.id}` },
          { label: this.diagram?.name },
        ]);

        this.nameDraft= this.diagram.name;

        // load diagram (empty or not)
        this.loadDiagram();
      },
      error: (error) => {
        console.error("Error fetching diagram:", error);
        this.messageService.add({
          severity: "error",
          summary: "Error",
          detail: "Cannot loading diagram",
        });
      },
    });
  }

  loadDiagram() {
    if (!this.diagram?.data) return;
    this.diagramData = {diagram: this.diagram.data, mode: 'init'};
  }

  addNode(name: string) {
    this.editor.addNode(name);
  }

  onAreaEventsChange(event: any) {
    this.diagramTouched = true;
  }

  // editor area handlers
  onEditorAreaCleared(event: any) {
    // handler
  }

  onSave($event: any) {
    let diagramData: any;
    try {
      diagramData = JSON.parse($event);
    } catch (e) {
      console.error("Error on parsing graph JSON", e);
      this.messageService.add({
        severity: "error",
        summary: "Error",
        detail: "Failed to save diagram",
      });
      return;
    }

    this.diagram = {
      id: this.diagram?.id || "",
      name: this.diagram?.name || "New Diagram",
      data: diagramData
    };

    /*
      Save diagram to endpoint
    */
    this.diagramService.updateDiagram(this.project!.id, this.diagram!)
      .subscribe({
        next: (response) => {
          this.diagram!.id = response.id;
          // url replacement with diagram id in case of new diagram
          this.router.navigate(
            [
              "/pages/diagram",
              this.project!.id,
              response.id
            ],
            { replaceUrl: true }   // <-- evita che la vecchia URL resti nella history
          );

          this.messageService.add({
            severity: "success",
            summary: "Success",
            detail: "Diagram saved successfully",
          });
        },
        error: (error) => {
          console.error("Error Saving diagram:", error);
          this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to save diagram",
          });
        },
      });
  }

  exportFormatHandler(json: string) {
    let parsedData: any;
    try {
      parsedData = JSON.parse(json);
    } catch (e) {
      console.error("Error on parsing graph JSON", e);
    }

    const diagramNodes: DiagramNode[] = parsedData.nodes.map((node: any) => ({
      id: node.id,
      label: node.label,
      shape: node.shape,
      payload: node.payload,
      weight: node.weight,
      position: node.position
    }));

    const diagramConnections: DiagramConnection[] = parsedData.connections.map(
      (connection: any) => ({
        id: connection.id,
        source: connection.source,
        target: connection.target,
        isLoop: connection.isLoop,
        weight: connection.weight,
        label: connection.label,
        payload: connection.payload
      })
    );

    const diagramData: DiagramData = {
      nodes: diagramNodes,
      connections: diagramConnections,
      viewport: parsedData.viewport
    };

    return JSON.stringify(diagramData, null, 2);
  }

  onNameChange($event: string) {
    if(this.diagram?.id === null) {
      // new diagram
      this.diagram.name = $event;
      this.changeName = false;
      this.updateBreadcrumb(this.diagram.name);
      return;
    }

    const oldName = this.diagram?.name;
    this.diagram!.name = $event;

    this.diagramService
      .updateDiagram(this.project!.id, this.diagram!)
      .subscribe({
        next: (response) => {
          // update breadcrumb
          this.updateBreadcrumb($event)
          this.changeName = false;
        },
        error: (error) => {
          console.error("Error updating title:", error);
          this.diagram!.name = oldName ?? "New Graph";
          this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to save title",
          });
        },
      });
  }

  onCancelName() {
    this.nameDraft = this.diagram!.name;
    this.changeName = false;
  }

  onGenerate($event: any) {
    this.diagramService.generateDiagram($event).subscribe({
      next: (res) => {
        if(res.data) {
          this.diagramData = {diagram: res.data, mode: "generate"}
        }
        this.messageService.add({
          severity: "success",
          summary: "Success",
          detail: "Diagram generated successfully",
        });
      },
      error: (error) => {
        console.error("Error generating diagram:", error);
          this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to generate diagram",
          });
      }
    });
  }

  onDockerDownload($event: any) {
    if(!this.diagram) return;
    this.exportService.exportDockerCompose(this.project!.id, this.diagram.id!).subscribe({
      next: (blob: Blob) => {
        // Crea un oggetto URL temporaneo per il blob
        const url = window.URL.createObjectURL(blob);
    
        // Crea un link temporaneo <a>
        const a = document.createElement('a');
        a.href = url;
        a.download = this.diagram?.name.replaceAll(' ', '_') + ".zip";
        document.body.appendChild(a);
        a.click(); // forza il download
        document.body.removeChild(a);
    
        // Rilascia l'oggetto URL
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Download error:', err);
        this.messageService.add({
          severity: "error",
          summary: "Error",
          detail: "Failed to generate Docker Compose export",
        });
      }
    });
  }

  private updateBreadcrumb(name: string) {
    // update breadcrumb
    this.items.update((items: MenuItem[]) => {
      const last = items.pop();
      if (last) {
        last.label = name;
        return [
          ...items,
          last
        ];
      }
      return items;
    });
  }

}

