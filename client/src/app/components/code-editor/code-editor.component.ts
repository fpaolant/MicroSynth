import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MonacoEditorModule } from "ngx-monaco-editor-v2";
import { FormsModule } from "@angular/forms";
import * as monaco from "monaco-editor";

@Component({
  selector: "app-code-editor",
  standalone: true,
  imports: [CommonModule, FormsModule, MonacoEditorModule],
  templateUrl: "./code-editor.component.html",
  styleUrl: "./code-editor.component.scss",
})
export class CodeEditorComponent implements AfterViewInit, OnDestroy {
  private _code: string = "";
  @Input()
    set code(value: string) {
      this._code = value;
    }
    
    get code(): string {
      return this._code;
    }

  @Input() language: string = "json";

  @Output() codeChange = new EventEmitter<string>();
  @Output() languageChange = new EventEmitter<string>();
  @Output() hasErrorsChange = new EventEmitter<boolean>();

  @ViewChild('editorContainer', { static: true }) editorContainerRef!: ElementRef<HTMLDivElement>;

  private editorInstance!: monaco.editor.IStandaloneCodeEditor;
  private resizeObserver!: ResizeObserver;

  editorOptions = {
    theme: "vs-dark",
    language: "json",
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    lineHeight: 20,
    fontSize: 14,
    wordWrap: "on",
    wrappingIndent: "indent",
  };


  ngAfterViewInit(): void {
    this.resizeObserver = new ResizeObserver(() => {
      if (this.editorInstance) {
        this.editorInstance.layout();
      }
    });

    this.resizeObserver.observe(this.editorContainerRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }

  onEditorInit(editor: monaco.editor.IStandaloneCodeEditor) {
    this.editorInstance = editor;

    editor.setValue(this._code || "")

    const model = editor.getModel();
    if (model) {
      monaco.editor.setModelLanguage(model, this.language || 'json');

      model.onDidChangeContent(() => {
        const updatedCode = model.getValue();
        this.codeChange.emit(updatedCode);
        //this.checkForErrors()
      });
    }
  }

  onLanguageChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    const selectedLanguage = target?.value;
    const model = this.editorInstance.getModel();
    if (model && selectedLanguage) {
      monaco.editor.setModelLanguage(model, selectedLanguage);
      this.languageChange.emit(selectedLanguage);
    }
  }

  private checkForErrors() {
    const model = this.editorInstance.getModel();
    if (!model) return;
    const markers = monaco.editor.getModelMarkers({ resource: model.uri });
    const hasErrors = markers.some(marker => marker.severity === monaco.MarkerSeverity.Error);
    //this.hasErrorsChange.emit(!hasErrors);
  }
}
