import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  ViewChild,
} from "@angular/core";
import {
  Connection,
  Node,
  HttpMethods,
  Languages,
  defaultEndpoint,
  ParameterTypes,
} from "../presets";
import { CommonModule } from "@angular/common";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { InputTextModule } from "primeng/inputtext";
import { ButtonModule } from "primeng/button";
import { FloatLabelModule } from "primeng/floatlabel";
import { InputNumberModule } from "primeng/inputnumber";
import { TextareaModule } from "primeng/textarea";
import { SelectButtonModule } from "primeng/selectbutton";
import { DividerModule } from "primeng/divider";
import { FieldsetModule } from "primeng/fieldset";
import { SelectModule } from "primeng/select";
import { FormBuilderService } from "./service/form-builder-service";
import { Endpoint } from "../types";
import { CheckboxModule } from "primeng/checkbox";
import { TooltipModule } from "primeng/tooltip";
import { Dialog } from "primeng/dialog";
import { EditorCodeDialog } from "./dialog/editor-code-dialog";
import { SliderModule } from 'primeng/slider';
import { debounceTime, Subscription } from "rxjs";

@Component({
  selector: "editor-context-menu",
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    InputNumberModule,
    ButtonModule,
    FloatLabelModule,
    TextareaModule,
    SelectButtonModule,
    DividerModule,
    FieldsetModule,
    SelectModule,
    CheckboxModule,
    TooltipModule,
    EditorCodeDialog,
    SliderModule
  ],
  templateUrl: "./editor-context-menu.component.html",
  styleUrl: "./editor-context-menu.component.scss",
})
export class EditorContextMenuComponent implements OnChanges {
  @ViewChild("codeEditorDialog") codeEditorDialog!: EditorCodeDialog;

  formConnection!: FormGroup;
  formNode!: FormGroup;
  httpMethods: string[] = HttpMethods;
  languages = Languages;
  parameterTypes = ParameterTypes;

  maxParameters = 10;
  maxResponses = 10;
  maxEndpoints = 10;

  @Input() node: Node | null = null;
  @Input() connection: Connection<Node, Node> | null = null;
  @Input() maxWeight: number = 1;

  @Output() nodeUpdated = new EventEmitter<Node>();
  @Output() connectionUpdated = new EventEmitter<Connection<Node, Node>>();

  availableEndpoints: Endpoint[] = [];
  showApiCall: boolean=false;
  readonlyMethod: boolean=false;

  private connectionSub?: Subscription;
  private nodeSub?: Subscription;


  constructor(
    private fb: FormBuilder,
    private fbService: FormBuilderService
  ) {}

  ngOnChanges(): void {
    // CONNECTION FORM
    if (this.connection) {
      // initialize form
      this.formConnection = this.fb.group({
        label: [this.connection.label, Validators.required],
        weight: [this.connection.weight ?? 0.1, Validators.required],
        payload: this.fbService.connectionPayloadForm(this.connection.payload)
      });    

      // determine if show api call section
      const api = this.connection.payload.apiCall;
      this.showApiCall = !!(api && (api.path || api.method || (api.parameterValues?.length ?? 0) > 0));

      // get node target endpoints
      this.availableEndpoints = this.connection.targetNode?.payload.endpoints || []
      
      const endpointPath = this.connection.payload.endpoint?.path;

      // if selected endpoint is no longer available
      if (endpointPath && !this.availableEndpoints.some(e => e.path === endpointPath)) {
        this.showApiCall = false;
        const payload = this.formConnection.get("payload") as FormGroup;
        const emptyApiCall = this.fbService.apiCall(undefined);
        payload.setControl("apiCall", emptyApiCall);
        payload.patchValue({ endpoint: null });
        
        emptyApiCall.markAsPristine();
        emptyApiCall.markAsUntouched();
        
        payload.markAsPristine();
        payload.markAsUntouched();
        return;
      } 

      // if apicall exists in connection
      if(this.connection.payload.endpoint) {
        const payload = this.formConnection.get("payload") as FormGroup;
        payload.setControl("apiCall", this.fbService.apiCall(this.connection.payload.apiCall));

        this.showApiCall = true
      }

      // clean up previous subscription if exists
      this.connectionSub?.unsubscribe();
      // subscribe to form changes
      this.connectionSub = this.formConnection.valueChanges
      .pipe(debounceTime(200)) // evita emissioni eccessive
      .subscribe(value => {
        if (!this.formConnection.valid) return;

        const updatedConnection: Connection<Node, Node> = {
          ...this.connection!,
          label: value.label,
          weight: value.weight,
          payload: {
            ...this.connection!.payload,
            apiCall: value.payload.apiCall,
            endpoint: value.payload.endpoint
          }
        };

        this.connectionUpdated.emit(updatedConnection);
      });
    }

    // NODE FORM
    if (this.node) {
      this.formNode = this.fb.group({
        label: [this.node.label, Validators.required],
        weight: [this.node.weight ?? 0.1, Validators.required],
        payload: this.fbService.nodePayloadForm(this.node.payload)
      });

      this.nodeSub?.unsubscribe();

      this.nodeSub = this.formNode.valueChanges
        .pipe(debounceTime(200))
        .subscribe(value => {
              if (!this.formNode.valid) return;
              if (!this.node) return;

              const updatedNode: Node = {
                ...this.node!,
                label: value.label,
                weight: value.weight,
                payload: {
                  ...this.node!.payload,
                  basePath: value.payload.basePath,
                  description: value.payload.description,
                  language: value.payload.language,
                  endpoints: value.payload.endpoints,
                  initiator: value.payload.initiator
                },
                hasInput: this.node.hasInput,
                addInput: this.node.addInput,
                removeInput: this.node.removeInput,
                hasOutput: this.node.hasOutput,
                addOutput: this.node.addOutput,
                removeOutput: this.node.removeOutput,
                hasControl: this.node.hasControl,
                addControl: this.node.addControl,
                removeControl: this.node.removeControl
              };

          this.nodeUpdated.emit(updatedNode);
        });
      
    }
  }

  selectConnectionEndpoint($event: { value: Endpoint }) {
    const endpointSelected = $event.value;
  this.showApiCall = true;

  const payload = this.formConnection.get("payload") as FormGroup;
  const apiCall = payload.get("apiCall") as FormGroup;
  const params = apiCall.get("parameterValues") as FormArray;

  // Aggiorna method e path
  apiCall.patchValue({
    method: endpointSelected.method,
    path: endpointSelected.path
  });

  // Aggiorna endpoint selezionato
  payload.patchValue({ endpoint: endpointSelected });

  // Parametri
  params.clear();

  endpointSelected.parameters.forEach(p => {
    params.push(
      this.fbService.parameterValue(
        { name: p.name, value: "" },
        p.required,
        true
      )
    );
  });

  this.readonlyMethod = true;
  this.formConnection.markAsTouched();
  }

  getInvalidControls() {
    const invalid = [];
    const controls = this.formConnection.controls;
  
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
  
    return invalid;
  }
  

  onSubmit() {
    if (this.connection != null && this.formConnection.valid) {
      this.connection = {
        ...this.connection,
        label: this.formConnection.value.label,
        weight: this.formConnection.value.weight,
        payload: { ...this.connection.payload, 
          apiCall: this.formConnection.value.payload.apiCall,
          endpoint: this.formConnection.value.payload.endpoint
        },
      };

      this.connectionUpdated.emit(this.connection);
      this.formConnection.markAsUntouched();
    }

    if (this.node != null && this.formNode.valid) {
      this.node = {
        ...this.node,
        label: this.formNode.value.label,
        weight: this.formNode.value.weight,
        payload: {
          ...this.node.payload,
          basePath: this.formNode.value.payload.basePath,
          description: this.formNode.value.payload.description,
          language: this.formNode.value.payload.language,
          endpoints: this.formNode.value.payload.endpoints,
          initiator: this.formNode.value.payload.initiator
        },
        hasInput: this.node.hasInput,
        addInput: this.node.addInput,
        removeInput: this.node.removeInput,
        hasOutput: this.node.hasOutput,
        addOutput: this.node.addOutput,
        removeOutput: this.node.removeOutput,
        hasControl: this.node.hasControl,
        addControl: this.node.addControl,
        removeControl: this.node.removeControl
      };
      this.nodeUpdated.emit(this.node);
      this.formNode.markAsUntouched();
    }
  }



  // ---------------------- NODE FORM ----------------------
  addParameter(endpointIndex: number) {
    const arr = this.formNode.get("payload.endpoints") as FormArray;
    const endpoint = arr.at(endpointIndex) as FormGroup;

    (endpoint.get("parameters") as FormArray).push(
      this.fbService.parameter({ name: "", type: "string", required: false })
    );
  }

  addResponse(endpointIndex: number) {
    const arr = this.formNode.get("payload.endpoints") as FormArray;
    const endpoint = arr.at(endpointIndex) as FormGroup;

    (endpoint.get("responses") as FormArray).push(
      this.fbService.apiResponse({
        status: 200,
        description: "",
        type: "application/json",
        content: null,
      })
    );
  }

  get endpoints(): FormArray {
    return this.formNode?.get("payload.endpoints") as FormArray;
  }

  endpointsParameters(index: number): FormArray {
    return this.endpoints.at(index).get("parameters") as FormArray;
  }

  endpointsResponses(index: number): FormArray {
    return this.endpoints.at(index).get("responses") as FormArray;
  }

  // Rimuovi endpoint
  removeEndpoint(index: number) {
    this.endpoints.removeAt(index);
  }

  // Aggiungi un nuovo endpoint
  addEndpoint() {
    const newEndpoint: Endpoint = defaultEndpoint();
    this.endpoints.push(this.fbService.endpoint(newEndpoint));
  }


  // Rimuovi parameter da un endpoint
  removeParameter(endpointIndex: number, paramIndex: number) {
    this.endpointsParameters(endpointIndex).removeAt(paramIndex);
  }

  // Rimuovi response da un endpoint
  removeResponse(endpointIndex: number, responseIndex: number) {
    this.endpointsResponses(endpointIndex).removeAt(responseIndex);
  }

  // ---------------------- CONNECTION FORM ----------------------
  addApiCallParameter(name="") {
    const params = this.formConnection.get("payload.apiCall.parameterValues") as FormArray;
    params.push(this.fbService.parameterValue({ name: name, value: "" }, false, false));
  }
  get apiCallParameters(): FormArray {
    return this.formConnection?.get("payload.apiCall.parameterValues") as FormArray;
  }
  // Rimuovi parameter dall'apiCall
  removeApiCallParameter(index: number) {
    this.apiCallParameters.removeAt(index);
  }

  openCodeEditorDialog(ei: number) {
    this.codeEditorDialog.data = {
      language: this.node?.payload.language || Languages[0],
      code: this.endpoints.at(ei).value.code ===''? "scrpt": '',
      params: this.endpointsParameters(ei).value,
      responses: this.endpointsResponses(ei).value
    };

    this.codeEditorDialog.codeChange = new EventEmitter<{ code: string }>();
    this.codeEditorDialog.codeChange.subscribe(({ code }) => {
      this.endpoints.at(ei).patchValue({ code });
    });
    

    this.codeEditorDialog.open();

  }

}
