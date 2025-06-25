import { CommonModule } from "@angular/common";
import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  Injector,
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

@Component({
  selector: "app-diagram",
  imports: [
    CommonModule,
    RouterModule,
    BreadcrumbModule,
    ButtonModule,
    EditorComponent,
    SplitterModule,
  ],
  templateUrl: "./diagram.component.html",
  styleUrl: "./diagram.component.scss",
})
export class DiagramPage implements OnInit, AfterViewInit {
  @ViewChild("editor") editor!: EditorComponent;

  projectService = inject(ProjectService);
  diagramService = inject(DiagramService);
  messageService = inject(MessageService);

  items = signal<MenuItem[]>([]);

  project: Project | null = null;
  diagram: Diagram | null = null;

  diagramTouched: boolean = false;

  diagramDataDraft: DiagramData = { nodes: [], connections: [] };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private injector: Injector
  ) {
    this.items.set([
      { icon: "pi pi-home", route: "/" },
      { label: "Projects", route: "/pages/projects" },
    ]);
  }

  ngOnInit(): void {
    // Get state from state
    const state = history.state;
    if (state.project) {
      this.loadProject(state.project.id);
    }
  }

  ngAfterViewInit(): void {
    // const el = this.container.nativeElement;
    // console.log('Container Element:', el);
    // if (el) {
    //   createEditor(el, this.injector);
    // }
  }

  loadProject(id: string) {
    this.projectService.getProject(id).subscribe({
      next: (project) => {
        this.project = project;

        // Recupera l'id del diagramma (se presente nei parametri)
        const diagramId = this.route.snapshot.paramMap.get("id");

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

        // load diagram (empty or not)
        this.loadDiagram();
      },
      error: (error) => {
        console.error("Error fetching project:", error);
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
    this.diagramDataDraft = this.diagram.data;
    // console.log("diagram to import", this.diagram.data)
  }

  addNode(name: string) {
    this.editor.addNode(name);
  }

  onAreaEventsChange(event: any) {
    this.diagramTouched = true;
    // Handle area events change
    switch (event.type) {
      case "nodepicked":
        console.log("Node clicked:", event.data);
        break;
    }
  }

  // editor area handlers
  onEditorAreaCleared(event: any) {
    
  }

  onSave($event: any) {
    console.log("Saving project:", this.project);
    console.log("Saving diagram:", this.diagram);
    console.log("Save clicked", $event);

    const parsedData = JSON.parse($event);

    const diagramNodes: DiagramNode[] = parsedData.nodes.map((node: any) => ({
      id: node.id,
      label: node.label,
      shape: node.shape,
      payload: { code: node.payload.code, language: node.payload.language },
      weight: node.weight,
    }));

    const diagramConnections: DiagramConnection[] = parsedData.connections.map(
      (connection: any) => ({
        id: connection.id,
        source: connection.source,
        target: connection.target,
        isLoop: connection.isLoop,
        weight: connection.weight,
        label: connection.label,
        payload: {
          code: connection.payload.code,
          language: connection.payload.language,
        },
      })
    );

    const diagramData: DiagramData = {
      nodes: diagramNodes,
      connections: diagramConnections,
    };

    this.diagram = {
      id: this.diagram?.id || "",
      name: this.diagram?.name || "New Diagram",
      data: diagramData,
    };

    /*
    
    */
    this.diagramService
      .updateDiagram(this.project!.id, this.diagram!)
      .subscribe({
        next: (response) => {
          console.log("Diagram updated successfully:", response);
          this.diagram!.id = response.id;

          this.messageService.add({
            severity: "success",
            summary: "Success",
            detail: "Diagram saved successfully",
          });
        },
        error: (error) => {
          console.error("Error updating diagram:", error);
          this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to save diagram",
          });
        },
      });
  }
}
