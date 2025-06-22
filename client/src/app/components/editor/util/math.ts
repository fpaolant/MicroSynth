import { NodeEditor } from "rete";
import { Connection, Node } from "../presets";
import { Schemes } from "../types";

type Position = { x: number, y: number }
type Size = { width: number, height: number }

export function computeEllipseIntersectionPoint(size: Size, end: Position, position: Position) {
  const radiusX = size.width / 2
  const radiusY = size.height / 2
  const centerX = position.x + size.width / 2;
  const centerY = position.y + size.height / 2;

  const dx = end.x - centerX;
  const dy = end.y - centerY;
  const distance = Math.sqrt(dx * dx + dy * dy);

  if (distance <= Math.min(radiusX, radiusY)) {
    return { x: end.x, y: end.y };
  }
  const angle = Math.atan2(dy, dx);

  const borderX = centerX + radiusX * Math.cos(angle);
  const borderY = centerY + radiusY * Math.sin(angle);

  return { x: borderX, y: borderY };
}

export function computeRectIntersectionPoint(size: Size, end: Position, position: Position): Position  {
  // Calculate the center of the rectangle
  const centerX = position.x + size.width / 2;
  const centerY = position.y + size.height / 2;

  // Calculate the direction vector of the line
  const directionX = end.x - centerX;
  const directionY = end.y - centerY;

  // Calculate the intersection points with the left, right, top, and bottom borders
  const tLeft = (position.x - centerX) / directionX;
  const tRight = (position.x + size.width - centerX) / directionX;
  const tTop = (position.y - centerY) / directionY;
  const tBottom = (position.y + size.height - centerY) / directionY;

  // Find the intersection point with the rectangle borders
  const tValues = [tLeft, tRight, tTop, tBottom].filter((t) => isFinite(t) && t >= 0 && t <= 1);

  if (tValues.length === 0) {
    // No intersection
    return position;
  }

  // Find the smallest positive t value (the closest intersection point)
  const minT = Math.min(...tValues);

  // Calculate the intersection point
  const intersectionX = centerX + minT * directionX;
  const intersectionY = centerY + minT * directionY;

  return { x: intersectionX, y: intersectionY };
}

export function getPerpendicularOffset({ x: x1, y: y1 }: Position, { x: x2, y: y2 }: Position, distance: number) {
  let dx = x2 - x1;
  let dy = y2 - y1;
  let len = Math.sqrt(dx * dx + dy * dy);
  let ux = dx / len;
  let uy = dy / len;
  const midpoint = { x: (x1 + x2) / 2, y: (y1 + y2) / 2 }

  let px = -uy * distance;
  let py = ux * distance;

  return { x: midpoint.x - px, y: midpoint.y - py };
}


/**
 * Check loops within graph
 * 
 * @param nodes
 * @param connections 
 * @returns true if found false elsewhere
 */
export function hasCycle(nodes: Node[], connections: Connection<Node, Node>[]) {
  const visited = new Set();
  const recStack = new Set();

  // Mappa degli archi: { nodeId: [connectedNodeId1, connectedNodeId2, ...] }
  const adjacencyList: Record<string, string[]> = {};

  for (const conn of connections) {
    if (!adjacencyList[conn.source]) adjacencyList[conn.source] = [];
    adjacencyList[conn.source].push(conn.target);
  }

  // DFS ricorsiva
  function dfs(nodeId:string) {
    if (recStack.has(nodeId)) return true; 
    if (visited.has(nodeId)) return false;

    visited.add(nodeId);
    recStack.add(nodeId);

    const neighbors = adjacencyList[nodeId] || [];
    for (const neighbor of neighbors) {
      if (dfs(neighbor)) return true;
    }

    recStack.delete(nodeId);
    return false;
  }

  for (const node of nodes) {
    if (!visited.has(node.id)) {
      if (dfs(node.id)) {
        return true;
      }
    }
  }

  return false;
}
