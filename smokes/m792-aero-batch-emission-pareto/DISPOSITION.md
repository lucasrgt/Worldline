# M792 experiment ledger

## Exact emission replay: rejected

Worldline `909e564b1080073a045cd50ac3cca2c7b9f60d45`, Aero
`4109b88aeac858b3f8ed0e863887e530939a0c9d` completed four clients.
The 16-entry CPU replay cache retained 5,441,408 bytes and did not meet the
stationary render-ratio target of 0.90: the two strata measured 0.945563 and
0.952021. One advancing stratum changed 50ms render-hitch counts from 0 to 1.
The implementation and hot-path hooks were removed; its exact source is
preserved at Aero `40e408aa715ebdc00130572403374b886e422915`.

This acquisition also exposed two observer defects. The first two within-client
comparisons included different terrain readiness; decoded frames show missing
background terrain in the first arm. The revised fixture renders every camera
for 120 frames before acquiring either arm. The original p99 and allocation
ratio calculations used integer division; those printed ratios are invalid.
The render ratios and integer hitch counts above are unaffected. A fractional
division regression self-test now runs before the official cycle. No result
was promoted and no threshold was relaxed.

Full original clients, profiles, captures and logs are retained under
`.worldline/evidence/m792-emission-replay-909e564b1`.

## Bounded adjacent draw runs: rejected

MegaCrusher contains 152 named groups. The original renderer starts and draws
one Tessellator per group. The successor retains the exact inner emission
method, joins only adjacent groups, and flushes before exceeding 65536 vertices.
It does not cache poses or geometry, reorder triangles, approximate transforms,
perform occlusion, or wait on the GPU. The same gain, tail, allocation and
zero-pixel acceptance limits remain in force.


Worldline `71fc97f36525d94e3d65cf08de7d84721f365713` with Aero
`5ced8e814ca721b0393889308626c68b07d38a30` preserved all 48 exact
frame pairs and removed 130 draw calls per retained frame with an 8 MiB
Tessellator buffer. It failed the unchanged performance contract: stationary
ratios were 0.992740 and 0.952826; moving off-on p99 regressed to 1.068691;
advancing on-off render ratio was 1.054178 and hitches increased from 9 to 11.
The implementation was removed. Complete metrics and 96 raw captures are in
`.worldline/evidence/m792-draw-runs-71fc97f36/complete-measurements.zip`.
The sibling runtime and measurements directory copies are incomplete after
disk exhaustion and must not be used as complete archives. Original runtime
evidence SHA-256 is 2f2ba2f37ce5a0a65f01a3571e1d85dfb65899ebf0aa1b6199ec814871cfe600.

## Exact multi-pose reuse: candidate

The controlled scene has 256 animated instances but 20 exact phases. The
existing single-representative optimization leaves most equivalent poses
unshared. The successor indexes at most 64 exact bundle/clip/raw-time identities
inside one dense batch. Standard non-transitioning playbacks only participate;
saturation and unknown cases retain normal rendering. Scratch is bounded to
1 MiB, no prior-frame geometry is retained, and triangle order is unchanged.
All acquisition scenes and numerical acceptance thresholds remain unchanged.


The first exact-pose acquisition at `631cc9a09267` failed: one entire RGBA
readback in each on-off client was zero, and the first client's render window
contained long stalls. Its complete measurements remain in
`.worldline/evidence/m792-multi-pose-631cc9a09.zip`. These are unresolved
acquisition failures, not evidence of correctness or a promoted gain.

The unchanged Aero candidate repeated at Worldline `e7ddeef7e` with GL error
checks. All 48 pairs were exact, all six scene/stratum render ratios improved
(0.717859..0.795192), p99 improved, and every 50ms hitch count was zero.
However, stationary on-off allocation ratio 1.224956 failed the unchanged 1.02
limit. The complete measurements remain in
`.worldline/evidence/m792-multi-pose-e7ddeef7e.zip`. This run is a failed gate.

The previous fixture warmed only the disabled path. The v4 fixture warms both
arms before steady-state measurement and attributes allocations separately to
animated and page flushes. No gain, p99, allocation or pixel threshold changed.
The candidate implementation remains exactly the same. Runtime console logs
now persist GL readback diagnostics. A warmed pass cannot explain the original
zero captures or qualify cold activation, so default promotion remains barred.
