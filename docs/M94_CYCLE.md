# M94 qualification cycle

`DefaultTtlPageRecoveryCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The first six requests reproduce M93's complete depletion. Restoration is
blocked until cache count is three, the expired counter advances exactly
`0 -> 1`, and capacity eviction remains zero. The first restore waits thirty
complete retained records after that exact expiry record; subsequent restores
remain thirty records after their preceding transition.

The 184-byte sidecar binds plan/root, expiry record/counters, and twelve
request/event/index triples. The runner reparses every M74/M78 record, exact
EOF and hashes, lifecycle, provenance, and clean worktrees. Diagnostic mode
cannot qualify or emit release evidence.

The canonical two-replica semantic SHA-256 is
`c2617f80713c9054acdf8ade17e4474a3a1ed275a2c092fc6d455363493acfcf`.
