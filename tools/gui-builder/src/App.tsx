import { Button, Surface } from '@heroui/react'
import { useMemo, useState } from 'react'
import { GuiCanvas } from './canvas/GuiCanvas.tsx'
import { ExportPanel } from './chrome/ExportPanel.tsx'
import { Inspector } from './chrome/Inspector.tsx'
import { Palette } from './chrome/Palette.tsx'
import { TreePanel } from './chrome/TreePanel.tsx'
import { emitJava } from './model/java.ts'
import { pack } from './model/pack.ts'
import { PRESETS, crusher } from './model/presets.ts'
import { compileSpec } from './model/spec.ts'
import {
  addChild,
  createLeaf,
  findById,
  hasKind,
  removeById,
  rename,
  targetParent,
} from './model/tree.ts'
import type { Kind, Widget } from './model/types.ts'

export default function App() {
  const [root, setRoot] = useState<Widget>(crusher)
  const [selectedId, setSelectedId] = useState<string | null>('screen')
  const boxes = useMemo(() => pack(root), [root])
  const selected = selectedId ? findById(root, selectedId) : null
  const compiled = useMemo(() => {
    try {
      return { spec: compileSpec(root), java: emitJava(root), error: null }
    } catch (error) {
      return { spec: null, java: '', error: error instanceof Error ? error.message : 'invalid tree' }
    }
  }, [root])

  function add(kind: Kind) {
    const parent = targetParent(root, selectedId)
    const next = addChild(root, parent.id, createLeaf(kind, root))
    setRoot(next)
  }

  return (
    <div className="h-svh flex flex-col bg-background text-foreground">
      <header className="flex items-center gap-3 px-4 py-2 border-b border-white/10">
        <h1 className="text-sm font-medium">Worldline GUI</h1>
        <div className="flex gap-1">
          {PRESETS.map((preset) => (
            <Button
              key={preset.id}
              size="sm"
              variant="tertiary"
              onPress={() => {
                const next = preset.build()
                setRoot(next)
                setSelectedId(next.id)
              }}
            >
              {preset.label}
            </Button>
          ))}
        </div>
      </header>
      <div className="flex-1 grid grid-cols-[280px_1fr_320px] min-h-0">
        <Surface className="flex flex-col gap-4 p-3 overflow-auto rounded-none border-r border-white/10">
          <section className="flex flex-col gap-2">
            <h2 className="text-xs uppercase tracking-wide opacity-60">Add</h2>
            <Palette disabledPlayer={hasKind(root, 'player')} onAdd={add} />
          </section>
          <section className="flex flex-col gap-2">
            <h2 className="text-xs uppercase tracking-wide opacity-60">Tree</h2>
            <TreePanel
              root={root}
              selectedId={selectedId}
              onSelect={setSelectedId}
              onRemove={(id) => {
                setRoot(removeById(root, id))
                if (selectedId === id) setSelectedId(root.id)
              }}
            />
          </section>
          <section className="flex flex-col gap-2">
            <h2 className="text-xs uppercase tracking-wide opacity-60">Inspector</h2>
            <Inspector
              widget={selected}
              onRename={(name) => {
                if (!selected || !name.trim()) return
                setRoot(rename(root, selected.id, name))
              }}
            />
          </section>
        </Surface>
        <div className="flex items-center justify-center bg-[#0a0a1a] overflow-auto">
          <GuiCanvas
            boxes={boxes}
            selectedId={selectedId}
            title={root.name}
            onSelect={(id) => setSelectedId(id ?? root.id)}
          />
        </div>
        <Surface className="p-3 overflow-auto rounded-none border-l border-white/10">
          {compiled.spec ? (
            <ExportPanel spec={compiled.spec} java={compiled.java} />
          ) : (
            <p className="text-sm text-red-400">{compiled.error}</p>
          )}
        </Surface>
      </div>
    </div>
  )
}
