# M110 qualification cycle

`CellSizeCeilingCycle` runs two balanced fresh-process pairs: raw thirty-three
then thirty-two, followed by thirty-two then thirty-three. A pair reuses only
nonce and fixed plan `(2,80,0)`. Minimum2, skip-individual false, pages=true,
cache=-1, rebuilds=-1, TTL100000, FPS maximum0, Aero pacing false, 300 warmup
frames/five seconds, and at least720 intervals/twelve seconds are frozen.

The M74 census and unified 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Both raw arms require effectiveSize32, queue16,
rendererCalls16, flush2, pageCalls1, direct0, rebuild0, cache1, eviction0,
immediate0, all identities, and M74 render/list0/0. The runner also requires
strict plan/camera, Aero-free server closure, clean shutdown, and cleanup.
Diagnostic mode cannot qualify.

The diagnostic pair retained 5875 raw-thirty-three and 6229 raw-thirty-two
records. Counts and all timing values are dynamic evidence outside
qualification; every retained diagnostic record met its exact structural state.

The canonical balanced run retained 5058/5325 records in the first
raw-thirty-three/raw-thirty-two pair and 6023/5534 records in the second
raw-thirty-two/raw-thirty-three pair. Every canonical record met the same exact
structural state; these counts and all timing summaries remain dynamic evidence.

The frozen semantic SHA-256 is
`4061454ff65c9ef06366042094e79fc165c26e91d6f3af2fcd7f04638a180c0e`.
