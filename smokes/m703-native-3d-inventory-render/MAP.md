<!-- worldline-map-schema=1 -->
<!-- boundary=native-3d-inventory-render -->
<!-- nonclaims=world-rendering,2d-item-icons,fluids,fire,rails,doors,signs,ladders,redstone-wire,repeaters,beds,piston-heads,moving-pistons,crossed-plants,torches,metadata-other-than-zero -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# M703 native 3D inventory render behavior map

## Boundary

This milestone proves the canonical metadata-zero inventory presentation for all 63
registered Beta 1.7.3 blocks accepted by the native
`RenderBlocks.renderItemIn3d` route. It does not generalize the result to world
rendering or to the 33 special and two-dimensional routes.

## Public contract

`NativeBlockRenderPlan` validates unique census subjects and the closed native 3D
render-type set `0, 10, 11, 13, 16`. `NativeBlockRenderFixture` rejects missing,
reordered, retyped, or zero-geometry observations and emits one canonical
`native-render` claim per row.

## Adapter path

```text
subjects.tsv
  -> B173BlockInventoryRender
  -> mapped RenderBlocks or official cv
  -> official terrain.png
  -> Minecraft Tessellator
  -> LWJGL Pbuffer 96x96
  -> RGBA frame plus geometry coverage
  -> NativeBlockRenderFixture
```

The adapter resolves the block registry and renderer methods using the frozen
`symbols.map`. Texture pixels always come from the independently hash-pinned
official client JAR.

## Differential oracle

The cycle starts four fresh JVMs: mapped A/B and official A/B. All four must emit
byte-identical canonical evidence for 63 rows. Code-source checks independently
prove that the mapped processes loaded `minecraft/bin` while the official
processes loaded `minecraft.jar`.

Frozen semantic signal: `family=native-3d-inventory-render,subjects=63,claims=63,render-types=5,processes=4,oracle=mapped-official-native-rgba`.

## Functional Census delta

The descriptor names exactly 63 `native-render` cells. No claim is made for the
special routes or for metadata variants not rendered by this canonical inventory
contract.

Frozen family SHA-256: `0000000000000000000000000000000000000000000000000000000000000000`.
