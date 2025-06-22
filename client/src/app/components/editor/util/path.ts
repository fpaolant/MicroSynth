import { BaseSchemes, ClassicPreset as Classic, NodeEditor } from 'rete';
import { getPerpendicularOffset } from './math';
import { AreaPlugin } from 'rete-area-plugin';

type Position = { x: number, y: number }

function hasMultipleConnections(editor: NodeEditor<BaseSchemes>, connection: Classic.Connection<Classic.Node, Classic.Node>): false | { index: number } {
  const source = connection.source
  const target = connection.target
  const sameRouteConnections = editor.getConnections().filter(connection => {
    return (connection.source === source && connection.target === target) || (connection.source === target && connection.target === source)
  });
  const multipleConnectionBetween = sameRouteConnections.length > 1
  const index = sameRouteConnections.indexOf(connection)

  return multipleConnectionBetween ? { index } : false
}

export function pathTransformer(editor: NodeEditor<BaseSchemes>, connection: Classic.Connection<Classic.Node, Classic.Node>) {
  const multiple  = hasMultipleConnections(editor, connection);

  if (!multiple) return (points: Position[]) => points

  return (points: Position[]) => {
    if (points.length === 2) {
      const start = points[0]
      const end = points[1]
      const mid = getPerpendicularOffset(start, end, 20)

      return [start, mid, end]
    }
    return points;
  }
}

/**
 * Forcing the area to update the sibling connections when a connection is removed
 */
export function useTransformerUpdater<A>(editor: NodeEditor<BaseSchemes>, area: AreaPlugin<BaseSchemes, A>) {
  editor.addPipe(async context => {
    if (context.type === 'connectionremoved') {
      await area.update('node', context.data.source);
    }
    return context;
  })
}