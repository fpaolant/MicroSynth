import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { Connection, Languages, Node } from "../presets";
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
import { Language } from "../types";

@Component({
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TagModule,
    ButtonModule,
    InputTextModule,
    PopoverModule,
    InputNumberModule,
    InputGroupModule,
    DialogModule,
    InputGroupAddonModule,
    CodeEditorComponent,
  ],
  selector: "app-custom-connection",
  templateUrl: "./custom-connection.component.html",
  styleUrls: ["./custom-connection.component.scss"],
})
export class CustomConnectionComponent implements OnInit, OnChanges {
  @ViewChild("popoverEditConn") overlayEdit!: Popover;
  @ViewChild("pathREf", { static: false }) pathREf!: ElementRef<SVGPathElement>;

  @Input() data!: Connection<Node, Node>;
  @Input() start: any;
  @Input() end: any;
  @Input() path: string = "";

  payloadDraft = "";
  payloadLanguageDraft: Language = Languages[0] as Language;

  hasPayloadCodeErrors: boolean = false;
  payloadDialogVisible: boolean = false;

  get point() {
    if (!this.pathREf) return { x: 0, y: 0 };
    const path = this.pathREf.nativeElement;
    const markerLength = 15;
    try {
      const totalLength = path.getTotalLength();
      const point = path.getPointAtLength((totalLength - markerLength) / 2);
      return point;
    } catch (e) {
      return { x: 0, y: 0 };
    }
  }

  ngOnInit(): void {
    this.payloadDraft = this.data.payload?.code || "{}";
    this.payloadLanguageDraft = this.data.payload?.language || Languages[0];
  }

  ngOnChanges(): void {
    if (!this.data.selected === false) this.overlayEdit?.hide();
  }

  onLanguageChange(event: string): void {
    this.payloadLanguageDraft = event as Language;
  }

  remove() {
    this.data.remove(this.data);
  }

  showPayloadDialog(): void {
    this.payloadDialogVisible = true;
  }

  cancelPayload(): void {
    this.payloadDialogVisible = false;
    // reset values
    this.payloadDraft = this.data.payload.code;
    this.payloadLanguageDraft = this.data.payload.language;
  }

  savePayload(): void {
    this.data.payload = {
      ...this.data.payload,
      code: this.payloadDraft,
      language: this.payloadLanguageDraft,
    };
    this.payloadDraft = this.data.payload.code;
    this.payloadLanguageDraft = this.data.payload.language;
    this.payloadDialogVisible = false;
    this.data.propertyChange("payload", this.data.payload);
  }
}
