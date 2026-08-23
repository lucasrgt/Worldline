import { DEFAULT_NAME, GROUP_KINDS, type Kind, type Widget } from './types.ts'

let nextId = 1

export function nid(): string {
  nextId += 1
  return `w${nextId}`
}

export function widget(kind: Kind, name: string, children: Widget[] = [], id = nid()): Widget {
  return { id, kind, name, children }
}

export function findById(root: Widget, id: string): Widget | null {
  if (root.id === id) return root
  for (const child of root.children) {
    const found = findById(child, id)
    if (found) return found
  }
  return null
}

export function parentOf(root: Widget, id: string): Widget | null {
  for (const child of root.children) {
    if (child.id === id) return root
    const found = parentOf(child, id)
    if (found) return found
  }
  return null
}

export function hasKind(root: Widget, kind: Kind): boolean {
  if (root.kind === kind) return true
  return root.children.some((child) => hasKind(child, kind))
}

export function updateById(root: Widget, id: string, fn: (current: Widget) => Widget): Widget {
  if (root.id === id) return fn(root)
  let changed = false
  const children = root.children.map((child) => {
    const next = updateById(child, id, fn)
    if (next !== child) changed = true
    return next
  })
  return changed ? { ...root, children } : root
}

export function removeById(root: Widget, id: string): Widget {
  if (root.id === id) return root
  const children = root.children.flatMap((child) => (
    child.id === id ? [] : [removeById(child, id)]
  ))
  const same = children.length === root.children.length
    && children.every((child, index) => child === root.children[index])
  return same ? root : { ...root, children }
}

export function addChild(root: Widget, parentId: string, child: Widget): Widget {
  if (child.kind === 'player' && hasKind(root, 'player')) {
    throw new Error('player inventory is already present')
  }
  return updateById(root, parentId, (current) => {
    if (!GROUP_KINDS.has(current.kind)) throw new Error('leaf cannot have children')
    return { ...current, children: [...current.children, child] }
  })
}

export function rename(root: Widget, id: string, name: string): Widget {
  const trimmed = name.trim()
  if (!trimmed) throw new Error('widget identity')
  return updateById(root, id, (current) => ({ ...current, name: trimmed }))
}

export function targetParent(root: Widget, selectedId: string | null): Widget {
  if (!selectedId) return root
  const selected = findById(root, selectedId)
  if (selected && GROUP_KINDS.has(selected.kind)) return selected
  return parentOf(root, selectedId ?? '') ?? root
}

export function createLeaf(kind: Kind, root: Widget): Widget {
  if (kind === 'screen') throw new Error('cannot add another screen')
  if (kind === 'player') return widget('player', 'player')
  const base = DEFAULT_NAME[kind]
  return widget(kind, unusedName(root, base))
}

function unusedName(root: Widget, base: string): string {
  const used = new Set<string>()
  walk(root, (current) => { used.add(current.name) })
  if (!used.has(base)) return base
  let index = 1
  while (used.has(`${base}.${index}`)) index += 1
  return `${base}.${index}`
}

function walk(root: Widget, visit: (widget: Widget) => void): void {
  visit(root)
  for (const child of root.children) walk(child, visit)
}
