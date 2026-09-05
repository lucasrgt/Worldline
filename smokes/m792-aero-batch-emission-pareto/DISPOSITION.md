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

## Bounded adjacent draw runs: candidate

MegaCrusher contains 152 named groups. The original renderer starts and draws
one Tessellator per group. The successor retains the exact inner emission
method, joins only adjacent groups, and flushes before exceeding 65536 vertices.
It does not cache poses or geometry, reorder triangles, approximate transforms,
perform occlusion, or wait on the GPU. The same gain, tail, allocation and
zero-pixel acceptance limits remain in force.
