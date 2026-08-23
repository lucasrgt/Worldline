import {
  GROUP_KINDS,
  LEAF_SIZE,
  PANEL_H,
  PANEL_W,
  PLAYER_H,
  PLAYER_OFFSET_Y,
  PLAYER_W,
  type Box,
  type Kind,
  type Widget,
} from './types.ts'

const RAIL_GAP = 8
const CELL_GAP = 0
const CONTENT_Y = 16

interface Measured {
  widget: Widget
  w: number
  h: number
  children: Measured[]
}

/** Places leaves on the 176x166 panel. row/column flatten; energy/tank hug the left. */
export function pack(root: Widget): Box[] {
  const boxes: Box[] = []
  const player = root.children.find((child) => child.kind === 'player')
  const sides = root.children.filter((child) => child.kind === 'energy' || child.kind === 'tank')
  const mains = root.children.filter(
    (child) => child.kind !== 'player' && child.kind !== 'energy' && child.kind !== 'tank',
  )
  let railX = 10
  const railY = CONTENT_Y
  for (const side of sides) {
    const measured = measure(side)
    place(measured, railX, railY, boxes)
    railX += measured.w + RAIL_GAP
  }
  if (mains.length > 0) {
    const column: Measured = {
      widget: { id: root.id, kind: 'column', name: root.name, children: mains },
      children: mains.map(measure),
      w: 0,
      h: 0,
    }
    column.w = Math.max(...column.children.map((child) => child.w), 0)
    column.h = column.children.reduce((sum, child, index) => (
      sum + child.h + (index > 0 ? CELL_GAP : 0)
    ), 0)
    const roomW = PANEL_W - 8 - railX
    const roomH = (player ? PLAYER_OFFSET_Y : PANEL_H) - CONTENT_Y - 4
    const x = railX + Math.max(0, Math.floor((roomW - column.w) / 2))
    const y = railY + Math.max(0, Math.floor((roomH - column.h) / 2))
    place(column, x, y, boxes)
  }
  if (player) {
    boxes.push({
      id: player.id,
      kind: 'player',
      name: player.name,
      x: Math.floor((PANEL_W - PLAYER_W) / 2),
      y: PANEL_H - PLAYER_OFFSET_Y,
      w: PLAYER_W,
      h: PLAYER_H,
    })
  }
  return boxes
}

function measure(widget: Widget): Measured {
  if (!GROUP_KINDS.has(widget.kind)) {
    const size = LEAF_SIZE[widget.kind as Exclude<Kind, 'screen' | 'row' | 'column'>]
    return { widget, w: size.w, h: size.h, children: [] }
  }
  const children = widget.children.filter((child) => child.kind !== 'player').map(measure)
  if (children.length === 0) return { widget, w: 0, h: 0, children }
  if (widget.kind === 'row') {
    return {
      widget,
      children,
      w: children.reduce((sum, child, index) => sum + child.w + (index > 0 ? CELL_GAP : 0), 0),
      h: Math.max(...children.map((child) => child.h)),
    }
  }
  return {
    widget,
    children,
    w: Math.max(...children.map((child) => child.w)),
    h: children.reduce((sum, child, index) => sum + child.h + (index > 0 ? CELL_GAP : 0), 0),
  }
}

function place(measured: Measured, x: number, y: number, boxes: Box[]): void {
  const { widget, children } = measured
  if (!GROUP_KINDS.has(widget.kind)) {
    boxes.push({ id: widget.id, kind: widget.kind, name: widget.name, x, y, w: measured.w, h: measured.h })
    return
  }
  if (widget.kind === 'row') {
    let cursor = x
    for (const child of children) {
      place(child, cursor, y + Math.floor((measured.h - child.h) / 2), boxes)
      cursor += child.w + CELL_GAP
    }
    return
  }
  let cursor = y
  for (const child of children) {
    place(child, x + Math.floor((measured.w - child.w) / 2), cursor, boxes)
    cursor += child.h + CELL_GAP
  }
}
