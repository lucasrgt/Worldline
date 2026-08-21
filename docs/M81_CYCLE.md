# M81 qualification cycle

`NaturalMultipageRebuildCycle` verifies the pinned Aero checkout and Aero-free
server closure, builds Aero in a disposable worktree, and runs two fresh
graphical client/modded-server replicas. Both use seed 81081, nonce 8108101,
and exact plan `(10,65,31)`, which crosses the Z=31/32 page boundary.

After retained record 300, one request asks the server to remove plan indices
zero and eight. The server validates exact coordinates, block and block-entity
types, and derived nonces before changing both blocks to air. The runner
requires one exact acknowledgement, two client air blocks, and one 40-byte
request/event sidecar written only after the complete census seals.

The M74 parser requires renderer/list counters zero, visible chunks positive,
state `0x1010`, and mask `0xffff` in every record. Identity calls are sixteen
before the transition and fourteen from it onward. The M78 parser requires
calls `16/16/2` then `14/14/2`, two cached pages/page calls, zero fallback, and
exactly two rebuilds in the transition record only.

Artifact lengths, SHA-256 markers, EOF, count, elapsed time, plan, and nonce
are cross-checked. Diagnostic mode runs one replica but cannot qualify or
produce release evidence.

The frozen semantic trace reproduces SHA-256
`f30116757d3fcf070289bdb013181744abdaf8da806426cc2efc76128484bc6d`.
