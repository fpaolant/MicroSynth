import { BaseSchemes, GetSchemes } from "rete";
import { Connection, Node } from "./presets";
import { AngularArea2D } from "rete-angular-plugin/19";
import { MinimapExtra } from "rete-minimap-plugin";

export type Schemes = GetSchemes<
  Node,
  Connection<Node, Node>
>;

export type Requires<Schemes extends BaseSchemes> =
  | { type: 'connectionpath', data: { payload: Schemes['Connection'], path?: string, points: Position[] } }

export type ExpectedScheme = GetSchemes<BaseSchemes['Node'] & { width: number, height: number }, BaseSchemes['Connection']>

export type AreaExtra = AngularArea2D<Schemes> | MinimapExtra;

export type Position = { x: number, y: number }
export type Shape = 'ellipse' | 'circle' | 'rect'
export type ShapeProps = { label: string, value: Shape, labelMaxLength: number, size: Size }
export type Size = { width: number, height: number }




export type Language = 'javascript'| 'java' | 'python';
export type HttpMethods = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
export type ApiResponseType = "application/json" | "application/text" | "binary";

export type ApiResponse = {
  status: number;
  description: string;
  type:ApiResponseType;
  content: any;
};


export type Parameter = {
  name: string;
  type: string;
  required: boolean;
} 

export type ParameterValue<T> = {
  name: string;
  value: T;
}



export type Endpoint = {
  path: string;
  summary: string;
  method: HttpMethods;
  parameters: Parameter[];
  responses: ApiResponse[];
  code: string;
}

export type ApiCall = {
  path: string;
  method: HttpMethods;
  parameterValues: ParameterValue<any>[];
}



export type NodePayload = {
  code: string,
  language: Language,
  type: 'controller',
  basePath: string,
  description: string,
  endpoints?: Endpoint[];
  initiator: boolean;
}

export type ConnectionPayload = {
  code: string,
  language: Language,
  apiCall?: ApiCall,
  endpoint?: Endpoint
}
