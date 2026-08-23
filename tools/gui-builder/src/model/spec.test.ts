import { describe, expect, it } from 'vitest'
import { emitJava } from './java.ts'
import { crusher, emptyScreen } from './presets.ts'
import { compileSpec, roleOf } from './spec.ts'
import { widget } from './tree.ts'

describe('compileSpec', () => {
  it('matches the Worldline crusher tree', () => {
    const spec = compileSpec(crusher())
    expect(spec.screen).toBe('crusher')
    expect(spec.nodes).toHaveLength(41)
    expect(spec.nodes[0]).toEqual({ role: 'screen', name: 'crusher', index: -1 })
    expect(spec.nodes[1]).toEqual({ role: 'slot', name: 'input', index: 0 })
    expect(spec.nodes[2]).toEqual({ role: 'progress', name: 'craft', index: -1 })
    expect(spec.nodes[3]).toEqual({ role: 'slot', name: 'output', index: 1 })
    expect(spec.nodes[4]).toEqual({ role: 'energy', name: 'energy', index: -1 })
    expect(spec.nodes[5]).toEqual({ role: 'slot', name: 'player.0', index: 2 })
    expect(spec.nodes[40]).toEqual({ role: 'slot', name: 'player.35', index: 37 })
  })

  it('omits player slots when playerInventory is absent', () => {
    const spec = compileSpec(widget('screen', 'bare', [widget('slot', 'input')]))
    expect(spec.nodes).toHaveLength(2)
    expect(spec.nodes[1]).toEqual({ role: 'slot', name: 'input', index: 0 })
  })

  it('rejects unknown builder types', () => {
    expect(() => roleOf('unknown')).toThrow(/unsupported/)
  })
})

describe('emitJava', () => {
  it('emits the crusher declaration', () => {
    expect(emitJava(crusher())).toBe(
      'Ui.screen("crusher",\n'
      + '    Ui.row("process",\n'
      + '        Ui.slot("input"),\n'
      + '        Ui.progress("craft"),\n'
      + '        Ui.slot("output")),\n'
      + '    Ui.energy("energy"),\n'
      + '    Ui.playerInventory());',
    )
  })

  it('emits a player-only screen', () => {
    expect(emitJava(emptyScreen())).toBe(
      'Ui.screen("menu",\n    Ui.playerInventory());',
    )
  })
})
