<!-- worldline-map-schema=1 -->
<!-- boundary=aero-ordered-template-performance -->
<!-- nonclaims=driver-gpu-time,cross-machine-portability,default-policy -->
<!-- frozen-trace=f3bf093dd71935bd450e22e0f47a305b848bbf1837136f6d5836472cd4e59643 -->

# M788 Aero ordered-template performance behavior map

## Fixture

One restored save contains 576 static MegaCrusher block entities split across
four towers and four chunks. Time, weather, unrelated entities, HUD, clouds,
view bob, interpolation, brightness, membership and route are fixed. Native
fixture rendering is suppressed so every measured frame has one controlled
producer.

## Actions

Eight fresh clients form four counterbalanced direct/template pairs. Each arm
warms 480 orbit, traverse, spin, teleport and close-inspection frames, then
settles its own render path for at least 60 frames. The direct arm executes the
exact Cell Page fallback 576 times. The template arm queues all 576 fixtures
and performs one controlled hot Cell Page flush. Each arm retains at least
1,200 frames and 20 seconds and captures 24 full RGBA checkpoints.

## Observations

Worldline records complete frame wall time, FPS, p50, p95, p99, client-thread
allocation, heap high-water mark, isolated fixture-render time, exact submitted
machine count, Cell Page template calls, rebuilds, direct fallbacks, cache size,
display-list guardrails, framebuffer bytes and 50 ms hitch rate.

## Oracle

Every retained frame must submit exactly 576 machines. The template arm must be
hot before measurement: 576 template calls per frame, zero direct fallbacks and
zero measurement-time rebuilds. Cross-arm pixels may differ only at bounded
same-arm raster-noise locations, and the workload must repeat within 2%.

The classification is `benefit-confirmed` when aggregate FPS improves at least
3%, p99 and allocation remain within 1.05, isolated render cost reaches 0.90 or
better, both launch-order strata qualify, at least three pairs win and the hitch
gate passes. A safe result with competing gains and costs is `mixed-tradeoff`; a material frame or
hitch regression is `regression-detected`. M787 remains the exact same-context
zero-pixel integrity prerequisite. This cycle does not claim driver GPU time,
cross-machine portability or a default-policy decision.

Signal: `scene=576-static-four-towers-four-chunks,membership=fixed,jvms=8-fresh-four-counterbalanced-pairs,route=orbit+traverse+spin+teleport,warm=480+arm-hot+template-stable,direct=576-exact-fallback-draws,template=576-queue+one-flush+final-hot-zero-fallback,flatten=off,submission=576-every-frame,rgba=cross-arm+repeatability,metrics=fps+p50+p95+p99+allocation+render+hitches,decision=benefit-confirmed-or-mixed-tradeoff-or-regression-detected`.
