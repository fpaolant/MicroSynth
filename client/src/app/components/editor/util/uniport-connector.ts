import { GetSchemes, getUID, NodeEditor } from 'rete';
import { BidirectFlow, Context, SocketData } from 'rete-connection-plugin';
import { Connection, defaultConnectionPayload, Node } from '../presets';
import { Schemes } from '../types';


type ClassicScheme = GetSchemes<Node, Connection<Node, Node> & { isLoop?: boolean }>

const looopConnectionsEnabled = false;
export class UniPortConnector<S extends ClassicScheme, K extends any[]> extends BidirectFlow<S, K> {

    


    constructor(connectionEvents: any, editor: NodeEditor<Schemes>) {
      super({
        makeConnection<K extends any[]>(initial: SocketData, socket: SocketData, context: Context<S, K>) {
          if(initial.nodeId === socket.nodeId && !looopConnectionsEnabled) return false;

          const sourceNode = editor.getNode(initial.nodeId);
          const targetNode = editor.getNode(socket.nodeId);
          
          context.editor.addConnection({
                      id: getUID(),
                      source: initial.nodeId,
                      sourceOutput: initial.key,
                      sourceNode: sourceNode,
                      target: socket.nodeId,
                      targetInput: socket.key,
                      targetNode: editor.getNode(initial.nodeId),
                      isLoop: (initial.nodeId === socket.nodeId && looopConnectionsEnabled)? true : false,
                      weight: 0.0,
                      label: 'getObject',
                      payload: defaultConnectionPayload('getObject'),
                      click: connectionEvents.click,
                      remove: connectionEvents.remove,
                      propertyChange: connectionEvents.propertyChange
                    } as S['Connection']);
                    
          return true
        }
      });


      
    }
  }