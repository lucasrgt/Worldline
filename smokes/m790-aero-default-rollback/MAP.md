<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cell-page-default-rollback -->
<!-- nonclaims=driver-gpu-time,cross-machine-portability,unbounded-memory-safety -->
<!-- frozen-trace=fcf61001a1250b0a6c31f3a75028a22db354b3275c1a61c62f31e5663c90b07a -->

# M790 Aero Cell Page default and rollback behavior map

## Fixture

One restored save contains 576 static MegaCrusher block entities split across
four towers and four chunks. Time, weather, unrelated entities, HUD, clouds,
view bob, interpolation, brightness, membership and route are fixed. Native
fixture rendering is suppressed so every measured frame has one controlled
Cell Page producer.

## Actions

Eight fresh clients form four counterbalanced rollback/default pairs. Both arms
warm 480 orbit, traverse, spin, teleport and close-inspection frames, then
settle the same ordered-template path for at least 60 frames. Each frame queues
all 576 fixtures and performs one controlled hot Cell Page flush. The rollback
arm explicitly sets `aero.becell.queueReuse=false`; the default arm omits that
property on the published Aero revision. Pool bounds remain 256 pages and 256
instances. Each arm retains at least 1,200 frames and 20 seconds and captures
24 full RGBA checkpoints.

## Observations

Worldline records frame wall time, FPS, p50, p95, p99, client-thread allocation,
heap high-water mark, isolated fixture-render time, exact submitted machines,
Cell Page calls, rebuilds, fallbacks, cache size, transient page allocations,
pool reuse, retained and discarded pages, display-list guardrails, framebuffer
bytes and 50 ms hitch rate.

## Oracle

Every retained frame must submit exactly 576 machines through the same hot
ordered-template path, ending with 576 template calls, zero rebuilds and zero
fallbacks. Cross-arm pixels may differ only at bounded same-arm raster-noise
locations, and the workload must repeat within 2%. The explicit rollback must
allocate transient pages and retain none. The unconfigured shipped default
must reuse pages, retain at most 256, discard none, and reduce transient page
creation by at least 95%.

The classification is `default-confirmed` when aggregate allocation falls at
least 10%, queue-page creation falls at least 95%, FPS remains at or above
0.97, p99 and render cost remain within 1.05, both launch-order strata pass,
at least three pairs win allocation and the hitch gate passes. A safe but
smaller or competing result is `mixed-tradeoff`; a material frame or hitch
regression is `regression-detected`. This cycle does not claim driver GPU time,
cross-machine portability or safety for an unbounded pool.

Signal: `scene=576-static-four-towers-four-chunks,membership=fixed,jvms=8-fresh-four-counterbalanced-pairs,route=orbit+traverse+spin+teleport,warm=480+both-arms-hot,baseline=rollback-false,reuse=shipped-default-no-flag,flatten=off,submission=576-every-frame,pool=default-owner-references-cleared+bounded,rgba=cross-arm+repeatability,metrics=fps+p50+p95+p99+allocation+render+pool+hitches,decision=default-confirmed-or-regression-detected`.
