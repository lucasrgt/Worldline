import { Button } from '@heroui/react'
import { GROUP_KINDS, type Widget } from '../model/types.ts'

export function TreePanel(props: {
  root: Widget
  selectedId: string | null
  onSelect: (id: string) => void
  onRemove: (id: string) => void
}) {
  return (
    <div className="flex flex-col gap-1 text-sm">
      <NodeRow
        widget={props.root}
        depth={0}
        selectedId={props.selectedId}
        onSelect={props.onSelect}
        onRemove={props.onRemove}
      />
    </div>
  )
}

function NodeRow(props: {
  widget: Widget
  depth: number
  selectedId: string | null
  onSelect: (id: string) => void
  onRemove: (id: string) => void
}) {
  const selected = props.widget.id === props.selectedId
  return (
    <div>
      <div
        className={`flex items-center gap-2 rounded-md px-2 py-1 cursor-pointer ${
          selected ? 'bg-accent/20 text-foreground' : 'hover:bg-white/5'
        }`}
        style={{ paddingLeft: 8 + props.depth * 14 }}
        onClick={() => props.onSelect(props.widget.id)}
      >
        <span className="font-mono text-[11px] opacity-60 uppercase w-16 shrink-0">
          {props.widget.kind}
        </span>
        <span className="truncate">{props.widget.name}</span>
        {props.widget.kind !== 'screen' && (
          <Button
            size="sm"
            variant="ghost"
            className="ml-auto h-6 min-w-6 px-1"
            onPress={() => props.onRemove(props.widget.id)}
          >
            ×
          </Button>
        )}
      </div>
      {GROUP_KINDS.has(props.widget.kind) && props.widget.children.map((child) => (
        <NodeRow
          key={child.id}
          widget={child}
          depth={props.depth + 1}
          selectedId={props.selectedId}
          onSelect={props.onSelect}
          onRemove={props.onRemove}
        />
      ))}
    </div>
  )
}
