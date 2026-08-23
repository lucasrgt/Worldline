import type { GameUiSpecJson, SpecNode, Widget } from './types.ts'

/** Mirrors worldline.api.GameUiSpec.roleOf / Ui.flatten / fromBuilder. */
const ROLE: Record<string, string> = {
  slot: 'slot',
  big_slot: 'slot',
  progress_arrow: 'progress',
  flame: 'progress',
  energy_bar: 'energy',
}

export function compileSpec(root: Widget): GameUiSpecJson {
  if (root.kind !== 'screen') throw new Error('root must be a screen')
  if (!root.name) throw new Error('ui spec screen')
  const parts: { type: string; name: string; slotType: string | null }[] = []
  const player = flatten(root.children, parts)
  const nodes: SpecNode[] = [{ role: 'screen', name: root.name, index: -1 }]
  const used = new Map<string, number>()
  let slotIndex = 0
  for (const part of parts) {
    if (part.type === 'separator') continue
    const role = roleOf(part.type)
    const name = unique(used, part.name || defaultName(part))
    nodes.push({ role, name, index: role === 'slot' ? slotIndex++ : -1 })
  }
  if (player) {
    for (let index = 0; index < 36; index++) {
      nodes.push({ role: 'slot', name: `player.${index}`, index: slotIndex++ })
    }
  }
  if (nodes.length < 2) throw new Error('ui spec nodes')
  return { screen: root.name, nodes }
}

function flatten(
  widgets: Widget[],
  parts: { type: string; name: string; slotType: string | null }[],
): boolean {
  let player = false
  for (const widget of widgets) {
    if (widget.kind === 'row' || widget.kind === 'column') {
      player = flatten(widget.children, parts) || player
    } else if (widget.kind === 'player') {
      player = true
    } else {
      parts.push({
        type: builderType(widget.kind, widget.name),
        name: widget.name,
        slotType: widget.kind === 'slot' ? widget.name : null,
      })
    }
  }
  return player
}

function builderType(kind: Widget['kind'], name: string): string {
  if (kind === 'slot') return 'slot'
  if (kind === 'progress') return 'progress_arrow'
  if (kind === 'energy') return 'energy_bar'
  if (kind === 'tank') return name.includes('gas') ? 'gas_tank' : 'fluid_tank'
  if (kind === 'search') return 'search_box'
  throw new Error(`unsupported builder component: ${kind}`)
}

export function roleOf(type: string): string {
  if (type in ROLE) return ROLE[type]
  if (type.includes('tank')) return 'tank'
  if (type.startsWith('search_box')) return 'search'
  if (type.startsWith('scrollbar')) return 'scroll'
  throw new Error(`unsupported builder component: ${type}`)
}

function defaultName(part: { type: string; name: string; slotType: string | null }): string {
  if (part.slotType) return part.slotType
  if (part.type === 'progress_arrow') return 'craft'
  if (part.type === 'energy_bar') return 'energy'
  if (part.type.includes('fluid')) return 'fluid'
  if (part.type.includes('gas')) return 'gas'
  if (part.type.startsWith('search_box')) return 'search'
  if (part.type.startsWith('scrollbar')) return 'scroll'
  return part.type
}

function unique(used: Map<string, number>, name: string): string {
  const count = used.get(name)
  used.set(name, count == null ? 1 : count + 1)
  return count == null ? name : `${name}.${count}`
}
