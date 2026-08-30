<!-- worldline-map-schema=1 -->
<!-- boundary=aero-page-cache-camera-prebake-matrix -->
<!-- nonclaims=no-default-promotion,no-off-thread-compilation,no-throughput-win-required -->
<!-- frozen-trace=ec6cf548fc2ae271688749d315e1bafc0feff90262015e8bd2cb7e63bfb01436 -->

# M775 Aero page cache and camera pre-bake behavior map

## Fixture

- AeroModelLib revision `f2242c74abf25f2b3081d29aec20e9fd6fb74431`
  renders one restored 576-machine solid tower.
- Four fresh rounds execute `direct`, `pages`, and `prebake` in balanced
  orders; every arm starts from the same copied save.
- The retained journey contains entry, walking, turning, one machine removal
  and restoration, teleport, and complete recovery.

## Actions and observations

1. `direct` disables cell pages and retains vanilla chunk scheduling.
2. `pages` enables cell pages while retaining vanilla chunk scheduling.
3. `prebake` keeps pages enabled and activates one camera-aware chunk rebuild
   per frame with look-ahead radius three.
4. Each arm records frame wall time, current-thread allocation, page calls,
   page rebuilds, maximum cached pages, actual chunk rebuild duration,
   scheduler-visible speculative pre-bakes, and dirty backlog.
5. Every phase must contribute at least sixty retained intervals, both
   optimized mechanisms must activate, and every final backlog must be zero.
6. The neutral paired hitch-rate gate compares `pages` with `prebake` at a
   50 ms threshold and a 5,000 ppm no-regression margin.

## Boundary

This milestone qualifies activation, accounting, recovery, and hitch safety.
It reports FPS, p99, and allocation descriptively; it does not require a
throughput win, enable either candidate by default, or claim off-thread GL
compilation.

Frozen trace: `v1|scene=restored-576|rounds=4|orders=direct-pages-prebake+prebake-pages-direct+pages-direct-prebake+prebake-direct-pages|journey=entry80+walk120+turn120+teleport130+drain150|mutation=remove150+restore180|pages=off-vs-on|prebake=off-vs-camera3-budget1|capture=wall+allocation+page+chunk+backlog|hitch=50ms+5000ppm|claims=activation+drain+hitch-safety`.

Expected signal: `scene=solid-576,rounds=4,arms=direct+pages+prebake,journey=entry+walk+turn+mutation+teleport+drain,pages=activated,prebake=activated,budget=1,backlog=zero,hitch=no-regression,allocation=measured`.
