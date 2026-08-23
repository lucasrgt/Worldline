import { useEffect, useRef } from 'react'
import type { Box } from '../model/types.ts'
import { PANEL_H, PANEL_W } from '../model/types.ts'
import { drawGui } from './draw.ts'

const SCALE = 3

export function GuiCanvas(props: {
  boxes: Box[]
  selectedId: string | null
  title: string
  onSelect: (id: string | null) => void
}) {
  const ref = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = ref.current
    if (!canvas) return
    canvas.width = PANEL_W * SCALE
    canvas.height = PANEL_H * SCALE
    const ctx = canvas.getContext('2d')
    if (!ctx) return
    drawGui(ctx, SCALE, props.boxes, props.selectedId, props.title)
  }, [props.boxes, props.selectedId, props.title])

  return (
    <canvas
      ref={ref}
      className="border border-white/10 shrink-0"
      style={{ imageRendering: 'pixelated', width: PANEL_W * SCALE, height: PANEL_H * SCALE }}
      onClick={(event) => {
        const rect = event.currentTarget.getBoundingClientRect()
        const x = (event.clientX - rect.left) / SCALE
        const y = (event.clientY - rect.top) / SCALE
        const hit = [...props.boxes].reverse().find((box) => (
          x >= box.x && x < box.x + box.w && y >= box.y && y < box.y + box.h
        ))
        props.onSelect(hit?.id ?? null)
      }}
    />
  )
}
