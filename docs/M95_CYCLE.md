# M95 qualification cycle

`PageCapacityThrashCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, one shared plan/nonce, strict camera, and an
Aero-free common/server closure.

The runner freezes maximum cache size three, rebuild budget eight, page TTL
100000, unlimited vanilla FPS, and disabled Aero pacing before either arm. It
reparses every M74 census record and every aligned 56-byte M95 page record,
requiring exact schema, identity, EOF, hashes, state, and monotonic cumulative
evictions. Diagnostic mode cannot qualify or write release evidence.

The two canonical replicas retained 5565 and 5549 complete samples. Both
proved cache3, pageCalls4, direct0, rebuild2, and a capacity-eviction delta of
exactly two on every sample. Clean client disconnect, server stop, disposable
worktree cleanup, and pinned-checkout provenance are mandatory.

The frozen semantic SHA-256 is
`4792da7a14435f7c4abeb761e4b22021b7afe0dc617b33422afba4d087035fa5`.
