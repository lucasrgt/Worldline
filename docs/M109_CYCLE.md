# M109 qualification cycle

`CellSizeFloorCycle` runs two balanced fresh-process pairs: raw zero then one,
followed by one then zero. A pair reuses only nonce and fixed plan `(2,80,0)`.
Minimum2, skip-individual false, pages=true, cache=-1, rebuilds=-1, TTL100000,
FPS maximum0, Aero pacing false, 300 warmup frames/five seconds, and at least
720 complete intervals/twelve seconds are frozen and validated.

The M74 census and unified 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Both raw arms require effectiveSize1, queue16,
rendererCalls16, flush2, pageCalls0, direct16, rebuild0, cache0, eviction0,
immediate0, all identities, and M74 render/list16/16. The runner also requires
strict plan/camera, Aero-free server closure, clean shutdown, and worktree
cleanup. Diagnostic mode cannot qualify.

The diagnostic pair retained 6888 raw-zero and 6150 raw-one records. Counts
and all timing values are dynamic evidence outside qualification. The
canonical arms retained 5433 raw-zero, 5072 raw-one, 5088 raw-one, and 6421
raw-zero records. Every retained record met its exact structural state.

The frozen semantic SHA-256 is
`d5ba4fa589d791959dca34158989889ea9d5c29942b6bc44fca7a18bb800a69e`.
