# M105 qualification cycle

`PairedCacheCapacityCycle` runs two balanced fresh-process pairs: cache1 then
unlimited, followed by unlimited then cache1. A pair reuses only its plan and
nonce. Pages=true, rebuilds=-1, TTL100000, FPS maximum0, Aero pacing false,
300 warmup frames/five seconds, and at least720 complete intervals/twelve
seconds are frozen and validated before evidence.

The shared M74 census and 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Cache1 requires rebuild4/cached1/eviction+4 in every
record. Unlimited requires rebuild0/cached4/eviction0. Both require queue16,
pageCalls4, immediate0, flush2, all sixteen identities, provenance, strict
camera, Aero-free server closure, clean shutdown, and worktree cleanup.
Diagnostic mode runs one pair but cannot qualify or emit release evidence.

The canonical arms retained 4768 cache1, 3891 unlimited, 5379 unlimited, and
5079 cache1 complete records. Every record met its exact structural state.
All timing values remain outside the release signature.

The frozen semantic SHA-256 is
`35da2fabb47ef902a2cbd7b92dc976771d9a80179b76322cf1f26edade4e5898`.
