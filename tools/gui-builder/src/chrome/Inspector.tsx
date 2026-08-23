import { Input, Label, TextField } from '@heroui/react'
import type { Widget } from '../model/types.ts'

export function Inspector(props: {
  widget: Widget | null
  onRename: (name: string) => void
}) {
  if (!props.widget) {
    return <p className="text-sm opacity-60">Select a node in the tree or on the canvas.</p>
  }
  return (
    <div className="flex flex-col gap-3">
      <p className="text-xs uppercase tracking-wide opacity-60">{props.widget.kind}</p>
      <TextField
        fullWidth
        name="name"
        value={props.widget.name}
        isReadOnly={props.widget.kind === 'player'}
        onChange={props.onRename}
      >
        <Label>Name</Label>
        <Input />
      </TextField>
    </div>
  )
}
