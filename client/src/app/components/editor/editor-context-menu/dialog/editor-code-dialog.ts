import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from "@angular/core";
import { DialogModule } from "primeng/dialog";
import { CodeEditorComponent } from "../../../code-editor/code-editor.component";
import { ButtonModule } from "primeng/button";
import { CommonModule } from "@angular/common";

@Component({
    standalone: true,
    imports: [
        ButtonModule,
        DialogModule,
        CodeEditorComponent,
        CommonModule
    ],
    selector: "editor-code-dialog",
    templateUrl: "./editor-code-dialog.html",
  })
  export class EditorCodeDialog implements OnInit, OnChanges {
    dialogVisible: boolean = false;

    @Input() data!: { code: string; language: string; params?: any[], responses?: any[] };
    @Input() languageChangesEnabled: boolean = false;
    @Output() codeChange = new EventEmitter<{ code: string }>();

    code: string = "";
    language: string = "javascript";
    hasCodeErrors: boolean = false;

    ngOnInit(): void {
        if (this.data) {
            console.log("EditorCodeDialog data init:", this.data);
            this.code = this.data.code;
            this.language = this.data.language;
        }  
    }

    ngOnChanges(): void {
        // Change detection logic here
        if (this.data) {
            console.log("EditorCodeDialog data changed:", this.data);
            this.code = this.data.code;
            this.language = this.data.language;
        }
    }

    onCodeChange(newCode: string): void {
        this.code = newCode;
    }

    open(): void {
        this.dialogVisible = true;
    }

    cancel(): void {
        this.dialogVisible = false;
    }
    
    save(): void {
        // Save logic here
        this.codeChange.emit({ code: this.code });
        this.dialogVisible = false;
    }

  }