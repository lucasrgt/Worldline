<!-- worldline-map-schema=1 -->
<!-- boundary=aero-loading-first-frame-integrity -->
<!-- nonclaims=loading-screen-redesign,prebake-during-loading,cross-jvm-raster-equivalence -->
<!-- frozen-trace=9f12f33025d44a37eb45767422069f15505a31a4589c18f5a2f7ff1d3f4651d5 -->

# M787 semantic map

## Boundary

M787 observes the official Beta 1.7.3 client loading display and the pinned
AeroModelLib StationAPI rendering boundary. It does not replace vanilla world
generation, loading, lighting, or simulation.

## Fixture

One restored save contains four independent sixteen-floor, nine-machine towers
across four chunks. The world is frozen after loading and unrelated terrain is
cleared from the capture boundary so the framebuffer contains only the 576
controlled fixture models.

## Loading oracle

Every measured fresh client must report the exact vanilla restored-world
sequence `Loading level`, blank reset, `Loading level`, `Building terrain`, and
`Simulating world for a bit`. The build and simulation stages occur exactly
once. `GameRenderer.renderWorld` must not execute while the loading trace is
active. This separates the vanilla loading transition from Aero prewarm and
chunk pre-bake, which only run from gameplay render frames.

## Cold-render oracle

Each fresh replica enables the production page queue with a rebuild budget of
eight and leaves flattening disabled. Rendering is not prewarmed. Every one of
240 frames must submit all 576 fixtures. Cell Pages must exercise direct
fallback on the first frame, compile pages, and converge to a non-empty cache
without ever dropping an instance. The final frame must use exactly 576 ordered
template calls and zero direct fallbacks.

Forty paired RGBA captures per client include the first sixteen visible frames
consecutively and a complete orbit, traverse, fast-spin, teleport, and close
inspection route. At each checkpoint Worldline installs explicit projection and
modelview matrices, captures Cell Pages, clears color and depth, and captures the
direct reference in the same OpenGL context. Two fresh replicas require zero
changed pixels across all 80 in-context pairs; cross-JVM raster state is not
used as a visual oracle.

## Ownership

AeroModelLib owns Cell Page implementation and optimization policy. Worldline
owns this differential official-runtime experiment, evidence, and promotion
boundary.

Signal: `scene=576-static-four-towers-four-chunks,sessions=2-fresh-paired-replicas,loading=vanilla-restored-loading+blank-reset+loading+building-once+simulating-once,render-during-loading=0,warm=none,frames=240,captures=40-paired-first16-consecutive+full-route,pages=in-context-budget8-flatten-off-then-clear-direct,camera=explicit-projection-modelview,submission=576-every-frame,hot=template-calls576+direct0,rgba=exact80of80,cold-fallback=present,cache=converged`.

Frozen semantic SHA-256: `9f12f33025d44a37eb45767422069f15505a31a4589c18f5a2f7ff1d3f4651d5`.
