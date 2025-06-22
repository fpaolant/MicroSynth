import { ClassicPreset } from "rete";
import { Shape } from "./types";
import { CurveFactory } from "d3-shape";
import { languages } from "monaco-editor";


export class Socket extends ClassicPreset.Socket {
  constructor(name: string, public shape: Shape = 'rect') {
    super(name);
  }
}



export class Connection<A extends Node, B extends Node> extends ClassicPreset.Connection<A, B> {
  selected?: boolean // custom property to mark selected connections
  payload: any = {
    code: '{}',
    languages: 'json',
  };
  label = '';
  weight = 0;

  click: (c: Connection<A, B>) => void;
  remove: (c: Connection<A, B>) => void;

  curve?: CurveFactory;

  constructor(events: { click: (data: Connection<A, B>) => void, remove: (data: Connection<A, B>) => void }, source: A, target: B, public isLoop: boolean = false) {
    super(source, 'default', target, 'default')
    this.click = events.click
    this.remove = events.remove
  }
}



export class Node extends ClassicPreset.Node {
  width = 80;
  height = 80;
  payload = {
    code: '{}',
    language: 'json',
  };
  weight = 0;

  remove: (c: Node) => void;
  duplicate: (c: Node) => void;

  constructor(label: string, public shape: Shape = 'circle', events: {remove: (data: Node) => void , duplicate: (data: Node) => void }) {
    super(label);

    this.addInput('default', new ClassicPreset.Input(new Socket('default')));
    this.addOutput('default', new ClassicPreset.Output(new Socket('default')));
    this.remove = events.remove
    this.duplicate = events.duplicate
  }
}



