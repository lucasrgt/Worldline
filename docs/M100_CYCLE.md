# M100 qualification cycle

`RebuildBudgetOneFallbackCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The runner rejects property drift and reparses every aligned census/sidecar
record. It requires strict 4/10 alternation, exact `direct↔pageCalls` pairing,
same-index M74 agreement, rebuild1, cache1, eviction delta one, both modes
present, and mode counts differing by at most one. Schema, identity, length,
EOF, hashes, lifecycle, provenance, clean shutdown, and worktree cleanup also
fail closed. Diagnostic mode cannot qualify or emit evidence.

The canonical replicas retained 4771 records (2386/2385 modes) and 4892
(2446/2446). Descriptive timing values remain dynamic and outside the release
signature.

The frozen semantic SHA-256 is
`322cccb6a7643bf79357d81d1c8b3ecf2bc0c7bcad170993ebbb01fc7fa8d76b`.
