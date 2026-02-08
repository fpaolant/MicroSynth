import { Injectable } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiCall, ApiResponse, ConnectionPayload, Endpoint, NodePayload, Parameter, ParameterValue } from '../../types';
import { defaultApiResponse, defaultEndpoint, defaultNodePayload, defaultParameter } from '../../presets';
import { InitEditableRow } from 'primeng/table';


@Injectable({ providedIn: 'root' })
export class FormBuilderService {

  constructor(private fb: FormBuilder) {}

  // ---------------------------
  // PARAMETERS
  // ---------------------------

  parameter(param: Parameter): FormGroup {
    return this.fb.group({
      name: [param.name || defaultParameter().name, Validators.required],
      type: [param.type || defaultParameter().type, Validators.required],
      required: [param.required || defaultParameter().required]
    });
  }

  parameterValue(p: ParameterValue<any>, isRequired = false, isSystem = false): FormGroup {
    return this.fb.group({
      name: [p.name, Validators.required],
      value: [
        p.value,
        isRequired ? Validators.required : []
      ],
      system: [isSystem]
    });
  }

  parameterValueArray(list: ParameterValue<any>[] = []): FormArray {
    return this.fb.array(list.map(v => this.parameterValue(v)));
  }


  // ---------------------------
  // API RESPONSES
  // ---------------------------

  apiResponse(r: ApiResponse): FormGroup {
    return this.fb.group({
      status: [r.status || defaultApiResponse().status, Validators.required],
      description: [r.description || defaultApiResponse().description],
      type: [r.type || defaultApiResponse().type, Validators.required],
      content: [r.content || defaultApiResponse().content]
    });
  }

  apiResponseArray(list: ApiResponse[] = []): FormArray {
    return this.fb.array(list.map(r => this.apiResponse(r)));
  }


  // ---------------------------
  // ENDPOINT
  // ---------------------------

  endpoint(e: Endpoint): FormGroup {
    return this.fb.group({
      path: [e.path || defaultEndpoint().path , Validators.required],
      summary: [e.summary || defaultEndpoint().summary],
      method: [e.method || defaultEndpoint().method, Validators.required],
      parameters: this.fb.array(e.parameters.map(p => this.parameter(p))),
      responses: this.apiResponseArray(e.responses),
      code: [e.code || defaultEndpoint().code]
    });
  }

  endpointArray(endpoints: Endpoint[] = []): FormArray {
    return this.fb.array(endpoints.map(e => this.endpoint(e)));
  }


  // ---------------------------
  // API CALL
  // ---------------------------

  apiCall(api: ApiCall | undefined): FormGroup {
    return this.fb.group({
      path: [api?.path?? '', Validators.required],
      method: [api?.method?? '', Validators.required],
      parameterValues: this.parameterValueArray(
        (api?.parameterValues ?? []).map(p => ({
          name: p.name,
          value: (p as any).value ?? ""   // converte sempre nel tipo corretto
        }))
      )
    });
  }


  // ---------------------------
  // NODE PAYLOAD
  // ---------------------------

  nodePayloadForm(payload: NodePayload): FormGroup {
    return this.fb.group({
      code: [payload.code || defaultNodePayload('').code],
      language: [payload.language || defaultNodePayload('').language, Validators.required],
      type: [payload.type || defaultNodePayload('').type, Validators.required],
      basePath: [
        payload.basePath || defaultNodePayload('').basePath,
        [
          Validators.required,
          Validators.pattern(/^\/([a-zA-Z0-9_-]+(\/[a-zA-Z0-9_-]+)*)?$/),
        ],
      ],
      description: [payload.description ?? defaultNodePayload('').description, Validators.maxLength(100)],
      endpoints: this.endpointArray(payload.endpoints ?? defaultNodePayload('').endpoints),
      initiator: [payload.initiator || defaultNodePayload('').initiator]
    });
  }


  // ---------------------------
  // CONNECTION PAYLOAD
  // ---------------------------

  connectionPayloadForm(payload: ConnectionPayload): FormGroup {
    return this.fb.group({
      code: [payload.code],
      language: [payload.language],
      apiCall: this.apiCall(payload.apiCall),
      endpoint: [payload.endpoint || null, Validators.required] 
    });
  }
}
