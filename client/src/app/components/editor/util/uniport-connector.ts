import { GetSchemes, getUID, NodeEditor } from 'rete';
import { BidirectFlow, Context, SocketData } from 'rete-connection-plugin';
import { Connection, Node } from '../presets';
import { Schemes } from '../types';


type ClassicScheme = GetSchemes<Node, Connection<Node, Node> & { isLoop?: boolean }>

const looopConnectionsEnabled = false;
export class UniPortConnector<S extends ClassicScheme, K extends any[]> extends BidirectFlow<S, K> {

    


    constructor(connectionEvents: any, editor: NodeEditor<Schemes>) {
      super({
        makeConnection<K extends any[]>(initial: SocketData, socket: SocketData, context: Context<S, K>) {
          if(initial.nodeId === socket.nodeId && !looopConnectionsEnabled) return false;

          const sourceName = editor.getNode(initial.nodeId)?.label || '';
          const targetName = editor.getNode(socket.nodeId)?.label || '';
          
          context.editor.addConnection({
                      id: getUID(),
                      source: initial.nodeId,
                      sourceOutput: initial.key,
                      target: socket.nodeId,
                      targetInput: socket.key,
                      isLoop: (initial.nodeId === socket.nodeId && looopConnectionsEnabled)? true : false,
                      weight: 0,
                      label: sourceName? sourceName + ' - ' + targetName : '',
                      payload: {
                        code: '{}',
                        language: 'json',
                      },
                      click: connectionEvents.click,
                      remove: connectionEvents.remove,
                      propertyChange: connectionEvents.propertyChange
                    } as S['Connection']);
          return true
        }
      });


      
    }
  }