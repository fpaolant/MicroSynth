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