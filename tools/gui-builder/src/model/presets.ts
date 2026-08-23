import { widget } from './tree.ts'
import type { Widget } from './types.ts'

export function crusher(): Widget {
  return widget('screen', 'crusher', [
    widget('row', 'process', [
      widget('slot', 'input'),
      widget('progress', 'craft'),
      widget('slot', 'output'),
    ]),
    widget('energy', 'energy'),
    widget('player', 'player'),
  ], 'screen')
}

export function chest(): Widget {
  const rows: Widget[] = []
  for (let row = 0; row < 3; row++) {
    const slots: Widget[] = []
    for (let col = 0; col < 9; col++) {
      slots.push(widget('slot', `chest.${row * 9 + col}`))
    }
    rows.push(widget('row', `row.${row}`, slots))
  }
  return widget('screen', 'chest', [
    widget('column', 'contents', rows),
    widget('player', 'player'),
  ], 'screen')
}

export function emptyScreen(): Widget {
  return widget('screen', 'menu', [widget('player', 'player')], 'screen')
}

export const PRESETS: { id: string; label: string; build: () => Widget }[] = [
  { id: 'crusher', label: 'Crusher', build: crusher },
  { id: 'chest', label: 'Chest', build: chest },
  { id: 'empty', label: 'Player only', build: emptyScreen },
]
