# M95 qualification cycle

`PageCapacityThrashCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, one shared plan/nonce, strict camera, and an
Aero-free common/server closure.

The runner freezes maximum cache size three, rebuild budget eight, page TTL
100000, unlimited vanilla FPS, and disabled Aero pacing before either arm. It
reparses every M74 census record and every aligned 56-byte M95 page record,
requiring exact schema, identity, EOF, hashes, state, and monotonic cumulative
evictions. Diagnostic mode cannot qualify or write release evidence.

Every retained sample proves cache3, pageCalls4, direct0, a rebuild count from
one through four, and a capacity-eviction delta equal to that sample's rebuild
count. The evidence records the dynamic distribution of all four modes. Clean
client disconnect, server stop, disposable worktree cleanup, and pinned-checkout
provenance are mandatory.

The frozen semantic SHA-256 is
`fc9cc66cafdba16acc0d1d076af30aad46e335509bebcd52b5f106bd5a6f138c`.
