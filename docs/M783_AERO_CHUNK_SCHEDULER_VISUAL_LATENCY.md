# M783-AERO-CHUNK-SCHEDULER-VISUAL-LATENCY Aero chunk scheduler visual-latency classification

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four counterbalanced pairs classify Aero camera-aware chunk scheduling against the page-cache baseline in fresh real clients, including direct visible dirty-to-rebuild latency, fresh world load, backlog drainage, hitch rate, FPS, p99, and allocation.

## Qualification cycle

M783 prepares one 576-machine save, then runs eight fresh clients over at least 600 retained frames each. Every client loads an identical copied world, then walks, turns, teleports, mutates visible, look-ahead, and background chunks, stops generating work after the acquisition window, and ends only after the backlog and visible-latency census drain. The evidence deterministically says promote or keep-disabled.

Expected signal: `scene=restored-576,pairs=4,jvms=8-fresh,window=600,fresh-load=per-jvm,route=walk+turn+teleport+mutation+settle+drain,pages=on,prebake=off-vs-budget1,visual-latency=measured,backlog=drained,world-reset=observed,hitch=classified,metrics=classified,decision=promote-or-keep-disabled`.

Frozen semantic SHA-256: `37903527839576445f009e6e6b140595131d069a1bd0d8eb3a1a739e2cf856f1`.
