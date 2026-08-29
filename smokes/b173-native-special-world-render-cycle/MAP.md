<!-- worldline-map-schema=1 -->
<!-- boundary=native-special-world-render -->
<!-- nonclaims=inventory-rendering,tile-entity-rendering,standing-sign,wall-sign,moving-piston,metadata-other-than-zero -->
<!-- frozen-trace=7e3bef933656e7d55330da386db48355163bdc9fbd2ae69510f913d65fb27227 -->

# Beta 1.7.3 native special world-render behavior map

## Boundary

This package proves canonical metadata-zero world presentation for all 30 registered
blocks whose official render type selects one of the 13 non-cube RenderBlocks paths.
The three render-type `-1` blocks are tile-entity renderer work and remain explicit.

## Public contract

`NativeWorldBlockRenderPlan` validates unique subjects and the closed special route
set. `NativeWorldBlockRenderFixture` rejects missing, reordered, retyped, or
zero-geometry observations and emits one `native-render` claim per row.

## Adapter path

```text
subjects.tsv -> deterministic IBlockAccess -> mapped RenderBlocks or official cv
  -> official terrain.png -> Minecraft Tessellator -> LWJGL Pbuffer 128x128
  -> RGBA frame plus geometry coverage -> NativeWorldBlockRenderFixture
```

The neighborhood contains the subject at the origin, stable support beneath it, and
the upper half required by doors. It is identical in mapped and official processes.

## Differential oracle

Four fresh JVMs run mapped A/B and official A/B. All must emit byte-identical
canonical evidence for 30 rows, with code-source proof for each renderer lane.

Frozen semantic signal: `family=native-special-world-render,subjects=30,claims=30,render-types=13,processes=4,oracle=mapped-official-native-rgba`.

## Functional Census delta

The descriptor names exactly 30 previously unknown `native-render` cells. It makes
no claim for standing signs, wall signs, or moving pistons, which use tile entities.

Frozen family SHA-256: `7e3bef933656e7d55330da386db48355163bdc9fbd2ae69510f913d65fb27227`.

