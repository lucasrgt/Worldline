# M98 qualification cycle

`ZeroCapacityFloorCycle` runs two fresh graphical-client/modded-server replicas
with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The client runtime rejects any `maxCachedPages` value other than zero. Every
aligned 56-byte record must nevertheless report cache1, four page calls, zero
fallback, four rebuilds, and eviction delta four. The runner reparses complete
M74/M98 artifacts with exact schema, identity, length, EOF, hashes, lifecycle,
provenance, clean shutdown, and worktree cleanup. Diagnostic mode cannot
qualify or emit evidence.

The canonical replicas retained 4133 and 3991 complete records, all rebuild4
with `rebuild3=0`.

The frozen semantic SHA-256 is
`0da3de05b8d5c493b974e04eaf1767e07f54b087f387badfdaf5dd48b6f1bb31`.
