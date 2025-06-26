import { Component, ElementRef, Input, OnChanges, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Connection, Node } from "../presets";
import { ButtonModule } from "primeng/button";
import { Popover, PopoverModule } from "primeng/popover";
import { InputNumberModule } from "primeng/inputnumber";
import { InputGroupAddonModule } from "primeng/inputgroupaddon";
import { InputGroupModule } from "primeng/inputgroup";
import { InputTextModule } from "primeng/inputtext";
import { FormsModule } from "@angular/forms";
import { TagModule } from "primeng/tag";
import { CodeEditorComponent } from "../../code-editor/code-editor.component";
import { DialogModule } from "primeng/dialog";




@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, TagModule, ButtonModule, InputTextModule, 
    PopoverModule, InputNumberModule, InputGroupModule, DialogModule,
    InputGroupAddonModule, CodeEditorComponent],
  selector: "app-custom-connection",
  templateUrl: "./custom-connection.component.html",
  styleUrls: ["./custom-connection.component.scss"]
})
export class CustomConnectionComponent implements OnInit, OnChanges {
  @ViewChild("popoverEditConn") overlayEdit!: Popover;
  @ViewChild('pathREf', { static: false }) pathREf!: ElementRef<SVGPathElement>;

  @Input() data!: Connection<Node, Node>;
  @Input() start: any;
  @Input() end: any;
  @Input() path: string = '';
  
  // drafts
  labelDraft = "";
  weightDraft = 0;
  labelMaxlength = 5;

  payloadDraft = "";
  payloadLanguageDraft = "json";
  hasPayloadCodeErrors: boolean = false;
  payloadDialogVisible: boolean = false;
  

  get point() {
    if (!this.pathREf) return { x: 0, y: 0 }
    const path = this.pathREf.nativeElement
    const markerLength = 15
    try {
      const totalLength = path.getTotalLength();
      const point = path.getPointAtLength((totalLength - markerLength) / 2);
      return point;
    } catch (e) {
      return { x: 0, y: 0 };
    }
  }


  ngOnInit(): void {
    this.labelDraft = this.data.label || "";
    this.weightDraft = this.data.weight || 0;
    this.payloadDraft = this.data.payload?.code || "{}";
    this.payloadLanguageDraft = this.data.payload?.language || "json";
    this.labelMaxlength = 5;
  }

  ngOnChanges(): void {
    if (!this.data.selected === false) this.overlayEdit?.hide();
  }

  remove() {
    this.data.remove(this.data)
  }

  showPayloadDialog() {
    this.payloadDialogVisible = true;
  }

  cancelPayload() {
    this.payloadDialogVisible = false;
    // reset values
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

  changeWeight() {
    this.data.propertyChange('weight', this.data.weight);
  }

}
