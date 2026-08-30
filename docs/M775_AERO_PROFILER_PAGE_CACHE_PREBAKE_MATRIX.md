# M775-AERO-PROFILER-PAGE-CACHE-PREBAKE-MATRIX Aero page cache and camera-aware pre-bake matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four counterbalanced three-arm rounds qualify Aero page-cache activation and the camera-aware one-rebuild pre-bake candidate in a restored 576-machine scene, with complete backlog drainage and a paired hitch-rate safety gate.

## Qualification cycle

M775 runs twelve fresh treatment clients after one template client. Each treatment follows entry, walk, turn, machine removal and restoration, teleport, and recovery phases while collecting complete frame intervals, thread allocation, page calls, page rebuilds, cached pages, real chunk rebuilds, speculative pre-bakes, and dirty backlog.

Expected signal: `scene=solid-576,rounds=4,arms=direct+pages+prebake,journey=entry+walk+turn+mutation+teleport+drain,pages=activated,prebake=activated,budget=1,backlog=zero,hitch=no-regression,allocation=measured`.

Frozen semantic SHA-256: `ec6cf548fc2ae271688749d315e1bafc0feff90262015e8bd2cb7e63bfb01436`.
