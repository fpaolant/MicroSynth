import { NodeEditor, Scope } from 'rete';
import { BaseSocketPosition } from 'rete-render-utils';
import { computeEllipseIntersectionPoint, computeRectIntersectionPoint } from './math';
import { Shape, ExpectedScheme, Position, Requires } from '../types';
import { LOOP_OFFSET, LOOP_SCALE } from '../constants';



export class ComputedSocketPosition<S extends ExpectedScheme, K> extends BaseSocketPosition<S, K> {
    constructor(private shape: Shape) {
      super()
    }
  
    public setShape(shape: Shape) {
      this.shape = shape
    }
  
    override attach(scope: Scope<Requires<S>, [K]>): void {
      super.attach(scope as Scope<never, [K]>)
      if (!this.area) return
      const editor = this.area.parentScope<NodeEditor<S>>(NodeEditor)
  
      scope.addPipe(context => {
        if (!this.area) return context
        if (!context || typeof context !== 'object' || !('type' in context)) return context
  
        const computeIntersectionPoint = this.shape === 'rect' ? computeRectIntersectionPoint : computeEllipseIntersectionPoint
  
        if (context.type === 'connectionpath') {
          const { source, target } = context.data.payload
          const sourceNode = editor.getNode(source)
          const targetNode = editor.getNode(target)
          const points = [...context.data.points]
          const sourceView = sourceNode && this.area.nodeViews.get(sourceNode.id)
          const targetView = targetNode && this.area.nodeViews.get(targetNode.id)
  
          if (!target) {
            if (!sourceView) return context
            points[0] = computeIntersectionPoint(sourceNode, points[1], sourceView.position)
  
            return {
              ...context,
              data: {
                ...context.data,
                points
              }
            }
          }
  
          if (!sourceNode || !targetNode) return context
  
          const isLoop = sourceNode === targetNode
  
          if (isLoop) {
            const distanceX = sourceNode.width / 2 + LOOP_OFFSET
            const distanceY = sourceNode.height / 2 + LOOP_OFFSET
            const p1 = { x: points[0].x + distanceX, y: points[0].y - LOOP_SCALE * sourceNode.height }
            const p2 = { x: points[0].x + LOOP_SCALE * sourceNode.width, y: points[0].y - distanceY }
  
            if (sourceView) points[0] = computeIntersectionPoint(sourceNode, p1, sourceView.position)
            if (targetView) points[1] = computeIntersectionPoint(targetNode, p2, targetView.position)
            points.splice(1, 0, p1, p2)
          } else {
            if (sourceView) {
              points[0] = computeIntersectionPoint(sourceNode, points[1], sourceView.position)
            }
            if (targetView) {
              points[1] = computeIntersectionPoint(targetNode, points[0], targetView.position)
            }
          }
          return {
            ...context,
            data: {
              ...context.data,
              points
            }
          }
        }
        return context
      })
    }
  
    async calculatePosition(nodeId: string): Promise<Position | null> {
      if (!this.area) return null
      const editor = this.area.parentScope<NodeEditor<S>>(NodeEditor)
      const node = editor.getNode(nodeId)
  
      if (!node) return null
  
      return {
        x: node.width / 2,
        y: node.height / 2
      }
    }
  }
  