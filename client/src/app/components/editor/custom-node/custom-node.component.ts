import {
  Component,
  Input,
  HostBinding,
  ChangeDetectorRef,
  OnChanges,
  OnInit,
  ViewChild,
} from "@angular/core";
import { ClassicPreset } from "rete";
import { CommonModule, KeyValue } from "@angular/common";
import { Popover, PopoverModule } from "primeng/popover";

//import { RefComponentDirective } from "../../../directives/ref.component";
import { RefDirective } from "rete-angular-plugin/19";
import { Node } from "../presets";
import { FormsModule } from "@angular/forms";
import { InputIconModule } from "primeng/inputicon";
import { InputGroupModule } from "primeng/inputgroup";
import { InputGroupAddonModule } from "primeng/inputgroupaddon";
import { ButtonModule } from "primeng/button";
import { TooltipModule } from "primeng/tooltip";
import { shapes } from "../constants";
import { DialogModule } from "primeng/dialog";
import { InputNumberModule } from "primeng/inputnumber";
import { CodeEditorComponent } from "../../code-editor/code-editor.component";

@Component({
  standalone: true,
  imports: [
    CommonModule,
    RefDirective,
    FormsModule,
    InputIconModule,
    InputNumberModule,
    PopoverModule,
    ButtonModule,
    DialogModule,
    TooltipModule,
    InputGroupModule,
    InputGroupAddonModule,
    CodeEditorComponent
  ],
  selector: "app-custom-node",
  templateUrl: "./custom-node.component.html",
  styleUrls: ["./custom-node.component.scss"],
  host: { "data-testid": "node" },
})
export class CustomNodeComponent implements OnInit, OnChanges {
  @ViewChild("popoverEdit") overlayEdit!: Popover;
  @Input() data!: ClassicPreset.Node & Node;
  @Input() emit!: (data: any) => void;
  @Input() rendered!: () => void;

  labelMaxlength = 5;

  seed = 0;

  // drafts
  labelDraft = "";
  weightDraft = 0;

  payloadDraft = "";
  payloadLanguageDraft = "json";
  hasPayloadCodeErrors: boolean = false;
  payloadDialogVisible: boolean = false;

  @HostBinding("class.selected") get selected() {
    return this.data.selected;
  }

  constructor(private cdr: ChangeDetectorRef) {
    //this.cdr.detach();
  }

  ngOnInit(): void {
    this.labelDraft = this.data.label || "";
    this.weightDraft = this.data.weight || 0;
    this.payloadDraft = this.data.payload?.code || "";
    this.payloadLanguageDraft = this.data.payload?.language || "json";
    this.labelMaxlength =
      shapes.find((s) => s.value === this.data.shape)?.labelMaxLength || 5;
  }

  ngOnChanges(): void {
    if (!this.data.selected === false) this.overlayEdit?.hide();
    this.cdr.detectChanges();
    requestAnimationFrame(() => this.rendered());
    this.seed++; // force render sockets
  }

  sortByIndex(
    a: KeyValue<string, { index?: number } | undefined>,
    b: KeyValue<string, { index?: number } | undefined>
  ): number {
    const ai = a.value?.index ?? 0;
    const bi = b.value?.index ?? 0;
    return ai - bi;
  }

  emitSocket = (props: any) => {
    const id = this.data.id;
    const ref = props.data.element;
    const sockets = [
      ["input", this.data.inputs["default"]?.socket] as const,
      ["output", this.data.outputs["default"]?.socket] as const,
    ];

    for (const [side, socket] of sockets) {
      this.emit({
        type: "render",
        data: {
          type: "socket",
          side,
          key: "default",
          nodeId: id,
          element: ref,
          payload: socket as any,
        },
      });
      requestAnimationFrame(() => {
        this.emit({
          type: "rendered",
          data: {
            type: "socket",
            side,
            key: "default",
            nodeId: id,
            element: ref,
            payload: socket as any,
          },
        });
      });
    }
  };

  duplicateNode() {
    this.data.duplicate(this.data);
    this.payloadDialogVisible = false;
  }

  removeNode() {
    this.data.remove(this.data);
    this.payloadDialogVisible = false;
  }

  showPayloadDialog() {
    this.payloadDialogVisible = true;
  }

  changeWeight() {
    this.data.propertyChange('weight', this.data.weight);
  }

  cancelPayload() {
    this.payloadDialogVisible = false;
    this.payloadDraft = this.data.payload.code;
    this.payloadLanguageDraft = this.data.payload.language
  }

  savePayload() {
    this.data.payload = { code: this.payloadDraft, language: this.payloadLanguageDraft };
    this.payloadDraft = this.data.payload.code;
    this.payloadLanguageDraft = this.data.payload.language;
    this.payloadDialogVisible = false;
    this.data.propertyChange('payload', this.data.payload);
  }

  saveLabel() {
    this.data.label = this.labelDraft
    this.data.propertyChange('label', this.data.label);
  }
  
}
