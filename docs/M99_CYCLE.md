# M99 qualification cycle

`RebuildBudgetFallbackCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The client rejects any cache maximum other than one, rebuild budget other than
two, or TTL other than 100000. Every aligned record must report M74 direct
counters 4/4 and M99 page state pageCalls2, directInstances4, rebuild2,
cache1, and eviction delta two. The runner reparses the complete census and
page sidecar with exact schema, identity, length, EOF, hashes, lifecycle,
provenance, clean shutdown, and worktree cleanup. Diagnostic mode cannot
qualify or emit evidence.

The canonical replicas retained 5003 and 4223 complete records; every one
followed the exact budget-two path. Descriptive timing values and their signs
remain dynamic and outside the release signature.

The frozen semantic SHA-256 is
`bc072d0104007b86828550033fb0aa3e84c179aa5caee84dcd22552c3c9a4ce7`.
