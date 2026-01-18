import { ClassicPreset } from "rete";
import { ApiCall, ApiResponse, ConnectionPayload, Endpoint, Language, NodePayload, Parameter, ParameterValue, Schemes, Shape } from "./types";
import { CurveFactory } from "d3-shape";


export class Socket extends ClassicPreset.Socket {
  constructor(name: string, public shape: Shape = 'rect') {
    super(name);
  }
}



export class Connection<A extends Node, B extends Node> extends ClassicPreset.Connection<A, B> {
  selected?: boolean // custom property to mark selected connections
  sourceNode: Node|undefined;
  targetNode: Node|undefined;
  payload: ConnectionPayload;
  label = '';
  weight = 0.0;

  click: (c: Connection<A, B>) => void;
  remove: (c: Connection<A, B>) => void;
  propertyChange: (key: string, value: any) => void;

  curve?: CurveFactory;

  constructor(
    events: { click: (data: Connection<A, B>) => void, 
      remove: (data: Connection<A, B>) => void, 
      propertyChange: (key: string, value: any) => void,
      getNodee:(id: Schemes['Node']['id']) =>  Schemes["Node"]
    }, 
    source: A, target: B, public isLoop: boolean = false) {
    super(source, 'default', target, 'default')
    this.sourceNode = source;
    this.targetNode = target;
    this.payload = defaultConnectionPayload(this.label);
    this.click = events.click;
    this.remove = events.remove;    
    this.propertyChange = events.propertyChange;
  }
}



export class Node extends ClassicPreset.Node {
  width = 80;
  height = 80;
  payload: NodePayload = defaultNodePayload(this.label);
  weight = 0.0;

  remove: (c: Node) => void;
  duplicate: (c: Node) => void;
  propertyChange: (key: string, value: any) => void;

  constructor(label: string, public shape: Shape = 'circle', 
    events: { remove: (data: Node) => void, duplicate: (data: Node) => void, propertyChange: (key: string, value: any) => void }) {
    super(label);

    this.addInput('default', new ClassicPreset.Input(new Socket('default')));
    this.addOutput('default', new ClassicPreset.Output(new Socket('default')));
    this.remove = events.remove;
    this.duplicate = events.duplicate;
    this.propertyChange = events.propertyChange;
  }
}




export const defaultParameterValue = (): ParameterValue<any> => ({
  name: 'id',
  value: '12345'
});

export const defaultApiCall = (): ApiCall => ({
  path: '/api/getObject',
  method: 'GET',
  parameterValues: [defaultParameterValue()]
});

export const defaultConnectionPayload = (label: string): ConnectionPayload  => {
  return {
    code: '',
    language: Languages[0] as Language,
    apiCall: undefined
  }
};




export const defaultParameter = (): Parameter => ({
  name: 'id',
  type: ParameterTypes[0],
  required: true
});

export const defaultApiResponse = (): ApiResponse => ({
  status: 200,
  description: 'success',
  type: 'application/json',
  content: {'message': 'success'}
});

export const defaultEndpoint = (): Endpoint => ({
  path: '/getObject',
  summary: 'getObject description',
  method: 'GET',
  parameters: [defaultParameter()],
  responses: [defaultApiResponse()],
  code: ''
});

export const defaultNodePayload = (nodeName: string): NodePayload => {
  return {
    code: '',
    language: Languages[0] as Language,
    type: 'controller',
    basePath: '/api',
    description: '',
    endpoints: [defaultEndpoint()],
    initiator: false
  }
};

export const HttpMethods = [
  "GET", "POST", "PUT", "PATCH", "DELETE"
];

export const Languages = [
  "javascript", "java", "python"
];

export const ParameterTypes = ["STRING", "INTEGER", "BOOLEAN", "JSON", ]
