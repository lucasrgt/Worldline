<!-- worldline-map-schema=1 -->
<!-- boundary=aero-high-memory-cell-pages -->
<!-- nonclaims=default-enable,weak-gpu-vram-equivalence,driver-memory-bytes -->
<!-- frozen-trace=7145d5c3f5de611f6a843b860a5439438e73f922d96bc762859d1ced4ccab3af -->

# M784 Aero high-memory Cell Page behavior map

## Fixture

One copied save contains 576 static MegaCrusher block entities split evenly
between four towers in four distinct chunks. The world time, weather, entities,
HUD, clouds, view bob and camera interpolation are frozen. Each fresh process
warms the complete deterministic route before evidence collection. The visual
oracle clears unrelated terrain color and depth before the first fixture mesh.
It submits every fixture through the production Cell Page queue with fixed
brightness and membership so dispatcher readiness cannot contaminate the A/B.
Both arms use a minimum page size of one to cover fragmented singleton pages.

## Actions

Eight clients run as four counterbalanced normal/high pairs. Each process
orbits all towers, traverses between chunks, spins rapidly,
teleports, and performs close inspection. Twenty-four full RGBA checkpoints are
captured at identical route positions.
Blank readbacks are retried at the same positions on the next route; more than
one route of blank checkpoints rejects the capture boundary.

## Observations

The cycle records complete frame intervals, p99, client-thread allocation,
heap high-water mark, Cell Page calls/rebuilds/direct fallback/cache size,
`aero.becell.flush` calls and nanoseconds, prewarm drainage, display-list live,
peak, denial and failure counts, full-frame pixels and 50 ms hitches.

## Oracle

The high-memory arm must activate flattened pages, TTL 1,800, rebuild budget
16 and page cap 4,096 while the normal arm retains false/600/8/unbounded.
Both arms must finish with bounded rebuild and direct-fallback rates,
normalized page-call drift within 10%, a drained prewarm queue and no
display-list eviction, denial or failure.
Cross-arm pixels may differ only at independently observed same-arm raster-noise
locations. Production opt-in promotion additionally requires at least 3% FPS
gain, p99 and allocation ratios no worse than 1.05, flush cost no worse than
0.90, both counterbalanced launch-order strata to meet those thresholds, at
least three of four pairs to win on FPS without p99 regression, and the paired
hitch-rate allowance. This blocks aggregate wins caused by one slow baseline.

This does not measure driver-owned bytes or claim safety on every weak GPU. It
can qualify high-memory mode as an explicit production option; it cannot make
that mode the default.

Signal: `scene=576-static-four-towers-four-chunks,membership=fixed,minInstances=1,jvms=8-fresh-four-counterbalanced-pairs,route=orbit+traverse+spin+teleport,memory=normal-vs-high,flatten=off-vs-on,cell-pages=stable,rgba=0-unexplained+blank-retry<=24,guardrails=bounded,order-strata=both+frame-wins>=3,metrics=classified,decision=promote-opt-in-or-keep-candidate`.
