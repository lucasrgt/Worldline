# M101 qualification cycle

`RebuildBudgetZeroDirectCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The client rejects any cache maximum other than one, rebuild budget other than
zero, or TTL other than 100000. Every aligned record must report M74 direct
counters 16/16 and M101 page state pageCalls0, directInstances16, rebuild0,
cache0, and eviction count zero. The runner reparses the complete census and
page sidecar with exact schema, identity, length, EOF, hashes, lifecycle,
provenance, clean shutdown, and worktree cleanup. Diagnostic mode cannot
qualify or emit evidence.

The canonical replicas retained 4490 and 4758 complete records; every one
followed the exact budget-zero direct path. All descriptive timing values
remain dynamic and outside the release signature.

The frozen semantic SHA-256 is
`8e0d8ae9c249c8f2967e0ac534c0ee7b7e79ff6a04bd7b407c89dcd2f5e7b0cd`.
