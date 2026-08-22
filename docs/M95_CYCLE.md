# M95 qualification cycle

`PageCapacityThrashCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, one shared plan/nonce, strict camera, and an
Aero-free common/server closure.

The runner freezes maximum cache size three, rebuild budget eight, page TTL
100000, unlimited vanilla FPS, and disabled Aero pacing before either arm. It
reparses every M74 census record and every aligned 56-byte M95 page record,
requiring exact schema, identity, EOF, hashes, state, and monotonic cumulative
evictions. Diagnostic mode cannot qualify or write release evidence.

Both canonical replicas must prove cache3, pageCalls4, direct0, rebuild3, and a
capacity-eviction delta of exactly three on every retained sample. Clean client
disconnect, server stop, disposable worktree cleanup, and pinned-checkout
provenance are mandatory.

The frozen semantic SHA-256 is
`1299a1e62338199b84fe116547d929116bdc23e793cf28a6e466f77e15bd3bed`.
