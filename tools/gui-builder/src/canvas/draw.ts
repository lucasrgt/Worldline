import { PANEL_H, PANEL_W, type Box } from '../model/types.ts'

type Color = readonly [number, number, number]

const MC = {
  BG: [198, 198, 198],
  BK: [0, 0, 0],
  WH: [255, 255, 255],
  DK: [85, 85, 85],
  SD: [55, 55, 55],
  SL: [139, 139, 139],
  ENERGY_A: [59, 251, 152],
  ENERGY_B: [54, 227, 138],
  GAUGE: [86, 0, 1],
} as const

function rgb(color: Color): string {
  return `rgb(${color[0]},${color[1]},${color[2]})`
}

export function drawGui(
  ctx: CanvasRenderingContext2D,
  scale: number,
  boxes: Box[],
  selectedId: string | null,
  title: string,
): void {
  ctx.imageSmoothingEnabled = false
  ctx.fillStyle = '#0a0a1a'
  ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height)
  panel(ctx, scale)
  const player = boxes.find((box) => box.kind === 'player')
  if (player) inventory(ctx, scale, player.x, player.y)
  ctx.fillStyle = '#404040'
  ctx.font = `300 ${9 * scale}px ui-monospace, Consolas, monospace`
  ctx.textBaseline = 'top'
  ctx.fillText(title, 8 * scale, 6 * scale)
  if (player) ctx.fillText('Inventory', player.x * scale, (player.y - 11) * scale)
  for (const box of boxes) {
    if (box.kind === 'player') continue
    drawBox(ctx, scale, box)
    if (box.id === selectedId) outline(ctx, scale, box, '#e94560')
  }
  if (player && player.id === selectedId) outline(ctx, scale, player, '#e94560')
}

function panel(ctx: CanvasRenderingContext2D, s: number): void {
  const fill = (x: number, y: number, w: number, h: number, c: Color) => {
    ctx.fillStyle = rgb(c)
    ctx.fillRect(x * s, y * s, w * s, h * s)
  }
  const hLine = (x: number, y: number, len: number, c: Color) => fill(x, y, len, 1, c)
  const pixel = (x: number, y: number, c: Color) => fill(x, y, 1, 1, c)
  const r = PANEL_W - 1
  const b = PANEL_H - 1
  fill(4, 4, PANEL_W - 8, PANEL_H - 8, MC.BG)
  hLine(2, 0, r - 4, MC.BK)
  pixel(1, 1, MC.BK); hLine(2, 1, r - 4, MC.WH); pixel(r - 2, 1, MC.BK)
  pixel(0, 2, MC.BK); pixel(1, 2, MC.WH); hLine(2, 2, r - 5, MC.WH)
  pixel(r - 3, 2, MC.WH); pixel(r - 2, 2, MC.BG); pixel(r - 1, 2, MC.BK)
  pixel(0, 3, MC.BK); pixel(1, 3, MC.WH); pixel(2, 3, MC.WH); pixel(3, 3, MC.WH)
  hLine(4, 3, r - 6, MC.BG); pixel(r - 2, 3, MC.DK); pixel(r - 1, 3, MC.DK); pixel(r, 3, MC.BK)
  for (let y = 4; y <= b - 4; y++) {
    pixel(0, y, MC.BK); pixel(1, y, MC.WH); pixel(2, y, MC.WH)
    hLine(3, y, r - 5, MC.BG)
    pixel(r - 2, y, MC.DK); pixel(r - 1, y, MC.DK); pixel(r, y, MC.BK)
  }
  pixel(0, b - 3, MC.BK); pixel(1, b - 3, MC.WH); pixel(2, b - 3, MC.WH)
  hLine(3, b - 3, r - 6, MC.BG)
  pixel(r - 3, b - 3, MC.DK); pixel(r - 2, b - 3, MC.DK); pixel(r - 1, b - 3, MC.DK); pixel(r, b - 3, MC.BK)
  pixel(1, b - 2, MC.BK); pixel(2, b - 2, MC.BG); hLine(3, b - 2, r - 3, MC.DK); pixel(r, b - 2, MC.BK)
  pixel(2, b - 1, MC.BK); hLine(3, b - 1, r - 4, MC.DK); pixel(r - 1, b - 1, MC.BK)
  hLine(3, b, r - 4, MC.BK)
}

function inventory(ctx: CanvasRenderingContext2D, s: number, x: number, y: number): void {
  for (let row = 0; row < 3; row++) {
    for (let col = 0; col < 9; col++) slot(ctx, s, x + col * 18, y + row * 18, 18, 18)
  }
  for (let col = 0; col < 9; col++) slot(ctx, s, x + col * 18, y + 58, 18, 18)
}

function slot(ctx: CanvasRenderingContext2D, s: number, x: number, y: number, w: number, h: number): void {
  fill(ctx, s, x, y, w - 1, 1, MC.SD)
  fill(ctx, s, x, y, 1, h - 1, MC.SD)
  fill(ctx, s, x, y + h - 1, w, 1, MC.WH)
  fill(ctx, s, x + w - 1, y, 1, h, MC.WH)
  fill(ctx, s, x + 1, y + 1, w - 2, h - 2, MC.SL)
  fill(ctx, s, x + w - 1, y, 1, 1, MC.SL)
}

function drawBox(ctx: CanvasRenderingContext2D, s: number, box: Box): void {
  if (box.kind === 'slot' || box.kind === 'search') {
    slot(ctx, s, box.x, box.y, box.w, box.h)
    return
  }
  if (box.kind === 'energy') {
    slot(ctx, s, box.x, box.y, box.w, box.h)
    for (let y = box.y + 1 + Math.floor(box.h * 0.3); y < box.y + box.h - 1; y++) {
      fill(ctx, s, box.x + 1, y, box.w - 2, 1, y % 2 === 0 ? MC.ENERGY_A : MC.ENERGY_B)
    }
    return
  }
  if (box.kind === 'tank') {
    slot(ctx, s, box.x, box.y, box.w, box.h)
    const innerH = box.h - 2
    const lines = Math.floor(innerH / 5) - 1
    for (let i = 1; i <= lines; i++) {
      const y = box.y + 1 + Math.floor(innerH * i / (lines + 1))
      fill(ctx, s, box.x + 1, y, i % 5 === 0 ? box.w - 2 : Math.floor((box.w - 2) / 2), 1, MC.GAUGE)
    }
    return
  }
  if (box.kind === 'progress') {
    fill(ctx, s, box.x + 1, box.y + 7, 14, 3, MC.SL)
    for (let i = 0; i < 8; i++) fill(ctx, s, box.x + 15 + i, box.y + 1 + i, 1, 15 - i * 2, MC.SL)
  }
}

function outline(ctx: CanvasRenderingContext2D, s: number, box: Box, color: string): void {
  ctx.strokeStyle = color
  ctx.lineWidth = 2
  ctx.setLineDash([4, 2])
  ctx.strokeRect(box.x * s - 1, box.y * s - 1, box.w * s + 2, box.h * s + 2)
  ctx.setLineDash([])
}

function fill(
  ctx: CanvasRenderingContext2D,
  s: number,
  x: number,
  y: number,
  w: number,
  h: number,
  color: Color,
): void {
  ctx.fillStyle = rgb(color)
  ctx.fillRect(x * s, y * s, w * s, h * s)
}
