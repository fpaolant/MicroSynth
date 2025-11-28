import { ShapeProps } from "./types"

export const LOOP_OFFSET = 65
export const LOOP_SCALE = 0.65

export const shapes: ShapeProps[] = [
    {
      label: 'Circle',
      value: 'circle',
      labelMaxLength: 10,
      size: { width: 80, height: 80 }
    },
    {
      label: 'Ellipse',
      value: 'ellipse',
      labelMaxLength: 6,
      size: { width: 140, height: 60 }
    },
    {
      label: 'Rectangle',
      value: 'rect',
      labelMaxLength: 10,
      size: { width: 140, height: 60 }
    }
  ]