<!-- worldline-map-schema=1 -->
<!-- boundary=native-tile-entity-render -->
<!-- nonclaims=tile-entity-text-content,animation-progress-other-than-zero,inventory-rendering -->
<!-- frozen-trace=0bbf481e13861e5c81960beb3eee4e1550547efc02caee06a6f2fbd6710a63ea -->

# Beta 1.7.3 native tile-entity render behavior map

## Boundary

This package proves canonical native presentation for every registered block whose
official render type is `-1`: moving piston, standing sign, and wall sign. Together
with the inventory and special-world families, it closes the registered-block
`native-render` matrix without pretending that tile entities use `RenderBlocks`
directly.

## Public contract

`NativeTileEntityRenderPlan` validates unique subjects and the closed sign/piston
route set. `NativeTileEntityRenderFixture` rejects missing, reordered, rerouted, or
zero-geometry observations and emits the correct archetype or singular
`native-render` claim for each row.

## Adapter path

```text
subjects.tsv -> concrete sign world or moving-piston state
  -> mapped TileEntitySpecialRenderer or official je subclass
  -> official sign.png or terrain.png -> LWJGL Pbuffer 128x128
  -> RGBA frame plus geometry coverage -> NativeTileEntityRenderFixture
```

Standing and wall signs share the sign renderer but use distinct canonical block
identity and metadata. The moving-piston route uses a canonical east-facing active
tile entity carrying stone and its native `RenderBlocks` bridge.

## Differential oracle

Four fresh JVMs run mapped A/B and official A/B. All must emit byte-identical
canonical evidence for three rows, with exact renderer code-source provenance.

Frozen semantic signal: `family=native-tile-entity-render,subjects=3,claims=3,render-types=1,processes=4,oracle=mapped-official-native-rgba`.

## Functional Census delta

The descriptor names the final three previously unknown `native-render` cells. It
makes no claim about sign text content or nonzero piston animation progress.

Frozen family SHA-256: `0bbf481e13861e5c81960beb3eee4e1550547efc02caee06a6f2fbd6710a63ea`.
