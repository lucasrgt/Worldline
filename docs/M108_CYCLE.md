# M108 qualification cycle

`PairedCellSizeCycle` runs two balanced fresh-process pairs: size two then
eight, followed by eight then two. A pair reuses only nonce and the fixed
aligned plan `(2,80,0)`. Minimum2, skip-individual false, pages=true, cache=-1,
rebuilds=-1, TTL100000, FPS maximum0, Aero pacing false, 300 warmup frames/five
seconds, and at least720 complete intervals/twelve seconds are frozen.

The M74 census and unified 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Size two requires pageCalls4/cache4; size eight requires
pageCalls1/cache1. Both require queue16/rendererCalls16/flush2/direct0/rebuild0,
eviction0/immediate0, all sixteen identities, M74 render/list0/0, strict fixed
X/Y/Z and camera, Aero-free server closure, clean shutdown, and worktree
cleanup. Diagnostic mode cannot qualify.

The diagnostic pair retained 5389 size-two and 1715 size-eight records. These
counts and all timing values are dynamic evidence outside qualification. The
canonical arms retained 4728 size-two, 4527 size-eight, 5035 size-eight, and
4688 size-two records. Every retained record met its exact structural state.

The frozen semantic SHA-256 is
`7bd2dd0f5f557a19c07eaf9d79978bfbac81aee3ad313df51ac504740b7c303d`.
