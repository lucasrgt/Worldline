# M790-AERO-DEFAULT-ROLLBACK Aero shipped Cell Page default and rollback

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Eight fresh clients in four counterbalanced pairs verify that the published Aero revision enables bounded Cell Page queue reuse without a flag and that the explicit false rollback remains functional, visually exact, and performance-safe.

## Qualification cycle

M790 restores the M789 576-machine four-chunk route, then runs four counterbalanced rollback/default pairs in fresh clients. Both arms warm and render the identical ordered Cell Page template path. The rollback arm explicitly sets aero.becell.queueReuse=false; the default arm omits the property entirely on the published Aero revision. Each arm measures at least 1,200 frames and 20 seconds after 480 route frames and cache convergence. Twenty-four full RGBA checkpoints per arm prove cross-arm output and same-arm repeatability. Pool counters must show zero reuse under rollback and bounded steady-state reuse under the unconfigured default, while frame, allocation, render, hitch, visual, cache, and workload gates remain satisfied.

Expected signal: `scene=576-static-four-towers-four-chunks,membership=fixed,jvms=8-fresh-four-counterbalanced-pairs,route=orbit+traverse+spin+teleport,warm=480+both-arms-hot,baseline=rollback-false,reuse=shipped-default-no-flag,flatten=off,submission=576-every-frame,pool=default-owner-references-cleared+bounded,rgba=cross-arm+repeatability,metrics=fps+p50+p95+p99+allocation+render+pool+hitches,decision=default-confirmed-or-regression-detected`.

Frozen semantic SHA-256: `fcf61001a1250b0a6c31f3a75028a22db354b3275c1a61c62f31e5663c90b07a`.
