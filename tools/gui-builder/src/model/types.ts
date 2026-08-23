export const PANEL_W = 176
export const PANEL_H = 166
export const PLAYER_OFFSET_Y = 83
export const PLAYER_W = 162
export const PLAYER_H = 76

export type Kind =
  | 'screen'
  | 'row'
  | 'column'
  | 'slot'
  | 'progress'
  | 'energy'
  | 'tank'
  | 'search'
  | 'player'

export interface Widget {
  id: string
  kind: Kind
  name: string
  children: Widget[]
}

export interface SpecNode {
  role: string
  name: string
  index: number
}

export interface GameUiSpecJson {
  screen: string
  nodes: SpecNode[]
}

export interface Box {
  id: string
  kind: Kind
  name: string
  x: number
  y: number
  w: number
  h: number
}

export const LEAF_SIZE: Record<Exclude<Kind, 'screen' | 'row' | 'column'>, { w: number; h: number }> = {
  slot: { w: 18, h: 18 },
  progress: { w: 24, h: 17 },
  energy: { w: 8, h: 54 },
  tank: { w: 18, h: 54 },
  search: { w: 88, h: 12 },
  player: { w: PLAYER_W, h: PLAYER_H },
}

export const GROUP_KINDS: ReadonlySet<Kind> = new Set(['screen', 'row', 'column'])

export const PALETTE: readonly Kind[] = [
  'row', 'column', 'slot', 'progress', 'energy', 'tank', 'search', 'player',
]

export const DEFAULT_NAME: Record<Kind, string> = {
  screen: 'screen',
  row: 'row',
  column: 'column',
  slot: 'slot',
  progress: 'craft',
  energy: 'energy',
  tank: 'fluid',
  search: 'search',
  player: 'player',
}
