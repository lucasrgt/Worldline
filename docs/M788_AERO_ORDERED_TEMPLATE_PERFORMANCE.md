# M788-AERO-ORDERED-TEMPLATE-PERFORMANCE Aero ordered Cell Page template performance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Eight fresh clients in four counterbalanced pairs classify the corrected ordered Cell Page template path against exact direct rendering in the same restored 576-machine, four-chunk route, preserving raster output and workload while reporting FPS, p50, p95, p99, allocation, isolated render time, display-list state, and hitch rate.

## Qualification cycle

M788 prepares one deterministic four-tower save, then loads it in four counterbalanced direct/template pairs. Every arm warms the full moving-camera route and its own render path before measuring at least 1,200 frames and 20 seconds. Native fixture rendering is suppressed; each frame renders exactly 576 machines either through the exact direct fallback or through 576 Cell Page submissions and one controlled hot flush. Twenty-four full RGBA checkpoints per arm prove cross-arm output and same-arm repeatability. The Worldline Profiler hitch gate classifies 50 ms events, while the evidence reports absolute and relative FPS, p50, p95, p99, allocation, isolated render cost, cache convergence, fallback count, and display-list guardrails. The outcome is benefit-confirmed, mixed-tradeoff, or regression-detected; no result is inferred from call counts alone.

Expected signal: `scene=576-static-four-towers-four-chunks,membership=fixed,jvms=8-fresh-four-counterbalanced-pairs,route=orbit+traverse+spin+teleport,warm=480+arm-hot+template-stable,direct=576-exact-fallback-draws,template=576-queue+one-flush+final-hot-zero-fallback,flatten=off,submission=576-every-frame,rgba=cross-arm+repeatability,metrics=fps+p50+p95+p99+allocation+render+hitches,decision=benefit-confirmed-or-mixed-tradeoff-or-regression-detected`.

Frozen semantic SHA-256: `f3bf093dd71935bd450e22e0f47a305b848bbf1837136f6d5836472cd4e59643`.
