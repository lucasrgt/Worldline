# M787-AERO-LOADING-FIRST-FRAME-INTEGRITY Aero loading and cold first-frame integrity

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Two fresh clients prove that restored-world loading follows the vanilla Loading/reset/Loading sequence with exactly one building and one simulation stage and without render-world or pre-bake work during loading, then require cold Cell Pages and the direct reference to render all 576 machines with exact paired RGBA pixels from the first visible frame through cache convergence and a complete moving-camera route.

## Qualification cycle

M787 prepares one deterministic four-tower save and loads it in two fresh replicas. Each client records the exact restored-world Loading level, blank reset, Loading level, Building terrain, and Simulating world for a bit sequence, rejects any render-world call before loading completes, disables speculative prewarm and culling, and begins without render warmup. For each capture it installs explicit projection and modelview matrices, submits all 576 fixtures through cold Cell Pages, captures RGBA, clears color and depth, renders the direct reference in the same OpenGL context, and captures the paired RGBA image. The first sixteen frames are consecutive and the remaining checkpoints cover orbit, traverse, spin, teleport, and close inspection. Both replicas require cold direct fallback, page-cache convergence to exactly 576 template calls and zero direct fallbacks on the final frame, and zero changed pixels across all eighty in-context comparisons.

Expected signal: `scene=576-static-four-towers-four-chunks,sessions=2-fresh-paired-replicas,loading=vanilla-restored-loading+blank-reset+loading+building-once+simulating-once,render-during-loading=0,warm=none,frames=240,captures=40-paired-first16-consecutive+full-route,pages=in-context-budget8-flatten-off-then-clear-direct,camera=explicit-projection-modelview,submission=576-every-frame,hot=template-calls576+direct0,rgba=exact80of80,cold-fallback=present,cache=converged`.

Frozen semantic SHA-256: `9f12f33025d44a37eb45767422069f15505a31a4589c18f5a2f7ff1d3f4651d5`.
