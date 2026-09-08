<!-- worldline-map-schema=1 -->
<!-- boundary=native-entity-render -->
<!-- nonclaims=shadow-and-fire-pass,animation-sequences,all-poses,all-skins,world-scene-rendering -->
<!-- frozen-trace=b971f0fde48598d64b749389f5b8db6fc89228e448b6f5fcad0025131cf7acfa -->

# Beta 1.7.3 native entity render behavior map

## Boundary

`b173-native-entity-render-cycle` qualifies the canonical body presentation of
every concrete EntityList subject. The public identity is `native-entity-render`;
the abstract registered EntityLiving type 48 cannot provide a native instance.

## Public contract

`worldline.testkit.NativeEntityRenderFixture#verify` accepts an ordered family
of unique concrete subjects and matching observations. Each observation contains
the registry class, native renderer class, nonempty geometry pixel count, and
128 by 128 RGBA hash. Evidence is equatable and exposes each exact
`subject#native-render` claim with its Census layer. Missing, reordered,
duplicated, empty, or misidentified observations fail closed.

## Controlled scene and oracle

The adapter reads only the existing hash-pinned Tiny mapping. It constructs
native entity classes, prepares canonical item, painting, fuse, falling-block,
slime, and fleece states, and asks the native RenderManager for each renderer.
The render-only scene supplies a full-bright empty chunk, official default
textures, zero partial tick, and a fixed camera scaled to the native envelope.
It invokes the native body renderer; it does not replace drawing code.

Four fresh JVMs capture mapped A/B and official A/B. Qualification requires
byte-identical family evidence, nonzero geometry, and exact native entity and
renderer code-source provenance. Official assets remain local to the pinned JAR.

## Functional Census delta

Frozen semantic signal:
`family=native-entity-render,subjects=23,processes=4,abstract=48:NOT_APPLICABLE,oracle=mapped-official-native-rgba`.

The target is all 23 concrete `native-render` cells and an explicit abstract
type-48 exception. The claims bind the matched four-process differential and must retain its exact signature.

Nonclaims: shadow and fire passes, animation sequences, every pose or skin,
world scene composition, and cross-GPU pixel portability.

Frozen family SHA-256:
`b971f0fde48598d64b749389f5b8db6fc89228e448b6f5fcad0025131cf7acfa`.
