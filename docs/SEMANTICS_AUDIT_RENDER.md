# Semantic Audit — Native Rendering

Domain inventory for `origin/main` `1755fa8` (v1.55.0 / M67). Yarn-to-MCP names confirmed
in `mappings.tiny`. Aero overlay mixins live under `worldline.aero.mixin`
and must not enter `SemanticCatalog.standard()`. The M18 Aero spike remains
a NON-CLAIM.

## Already catalogued

`COMPILE_CHUNKS` = `RenderGlobal.updateRenderers`. `DISPLAY`,
`ENTITY_RENDERER`, `RENDERER_UPDATE` (`EntityRenderer.updateRenderer`),
`EFFECT_RENDERER`, `EFFECT_TICK` (`updateEffects`). `EFFECT_UPDATE` is
`B173Observation.rendererTick`, not particle update.

## Promoted

Tessellator class, instance, `startDrawingQuads`, `setColorRGBA`,
`addVertex`, `draw` from `smokes/m10-native-render/symbols.map`.
`LOAD_RENDERERS` = `RenderGlobal.loadRenderers`. `CAMERA_RENDER` =
`EntityRenderer.updateCameraAndRender`. `CHUNK_REBUILD` =
`WorldRenderer.updateRenderer` `()V` (M14 asserts `rebuilds > 0`). Not
`RENDERER_UPDATE`. No additional render roles in the M48-M67 packet pass.

## Inventory, not catalog

| Yarn intercept | MCP subject | Suggested role |
| --- | --- | --- |
| `sortChunks` | `markRenderersForNewPosition` | `SORT_RENDERERS` |
| `markDirty(IIIIII)` | `func_949_a` | `MARK_BLOCKS_FOR_UPDATE` |
| `ChunkBuilder.invalidate` | `WorldRenderer.markDirty()V` | `CHUNK_INVALIDATE` |
| `notifyAmbientDarknessChanged` | unresolved | — |

The Aero adapter manifest lists nine oracled `worldline/aero/` sites.
`SORT_RENDERERS`, `MARK_BLOCKS_FOR_UPDATE`, `CHUNK_INVALIDATE`, and
`notifyAmbientDarknessChanged` stay inventory: no non-zero oracle.

## Conflicts

Yarn `WorldRenderer` is MCP `RenderGlobal`. Yarn `ChunkBuilder` is MCP
`WorldRenderer`. `CHUNK_REBUILD` must not reuse `RENDERER_UPDATE`.
