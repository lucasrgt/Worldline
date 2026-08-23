import type { Widget } from './types.ts'

/** Emits the Worldline Ui.screen declaration for this tree. */
export function emitJava(root: Widget): string {
  if (root.kind !== 'screen') throw new Error('root must be a screen')
  if (root.children.length === 0) return `Ui.screen("${escape(root.name)}");`
  const inner = root.children.map((child) => emitWidget(child, '    ')).join(',\n')
  return `Ui.screen("${escape(root.name)}",\n${inner});`
}

function emitWidget(widget: Widget, indent: string): string {
  if (widget.kind === 'player') return `${indent}Ui.playerInventory()`
  if (widget.kind === 'row' || widget.kind === 'column') {
    if (widget.children.length === 0) {
      return `${indent}Ui.${widget.kind}("${escape(widget.name)}")`
    }
    const inner = widget.children.map((child) => emitWidget(child, `${indent}    `)).join(',\n')
    return `${indent}Ui.${widget.kind}("${escape(widget.name)}",\n${inner})`
  }
  return `${indent}Ui.${widget.kind}("${escape(widget.name)}")`
}

function escape(value: string): string {
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')
}
