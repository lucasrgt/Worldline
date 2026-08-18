# M97 qualification cycle

`PageCapacityOneThrashCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

Every aligned 56-byte M97 page record is reparsed against its complete M74
census. The gate requires cache1, four page calls, zero direct fallback,
exactly four rebuilds and cumulative eviction delta four after the first
retained record. Schema, identity, length, EOF, hashes, lifecycle, provenance,
clean shutdown, and worktree cleanup are fail-closed. Diagnostic mode cannot
qualify or emit release evidence.

The canonical replicas retained 5067 and 4581 complete records. Both reported
zero rebuild3 records and all records in rebuild4 mode.

The frozen semantic SHA-256 is
`93c51ccdd98d0abd4e6da174f6ea76d8ca10ddb31cfed965117945473a39c551`.
