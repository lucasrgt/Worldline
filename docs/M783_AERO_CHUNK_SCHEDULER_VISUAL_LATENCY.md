# M783-AERO-CHUNK-SCHEDULER-VISUAL-LATENCY Aero chunk scheduler visual-latency classification

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four counterbalanced pairs classify Aero camera-aware chunk scheduling against the page-cache baseline in fresh real clients, including direct visible dirty-to-rebuild latency, a midpoint world reload, backlog drainage, hitch rate, FPS, p99, and allocation.

## Qualification cycle

M783 prepares one 576-machine save, then runs eight fresh clients over 2,400 retained frames each. Every route walks, turns, teleports, mutates visible, look-ahead, and background chunks, reloads the world in-process at the midpoint, and ends only after the backlog and visible-latency census drain. The evidence deterministically says promote or keep-disabled.

Expected signal: `scene=restored-576,pairs=4,jvms=8-fresh,window=2400,reload=midpoint,route=walk+turn+teleport+mutation+settle,pages=on,prebake=off-vs-budget1,visual-latency=measured,backlog=drained,world-reset=observed,hitch=classified,metrics=classified,decision=promote-or-keep-disabled`.

Frozen semantic SHA-256: `97cddf41d7eb1df7d8f0c3f2c7af129cb5e41c21d501b33361ddf02ee14dbce6`.
