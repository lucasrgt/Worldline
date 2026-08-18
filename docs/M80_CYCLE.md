# M80 qualification cycle

`NaturalMembershipRebuildCycle` verifies the pinned Aero checkout and the
Aero-free server closure, builds Aero in a disposable worktree, and runs two
fresh graphical client/modded-server replicas. The second replica receives the
first replica's exact plan; nonce, camera, heap, page budget, frame limits, and
census window are equal.

After retained record 300, the client requests removal of exact plan cell zero.
The server validates identifier, coordinates, BE type, and derived nonce before
setting the block to air. The runner requires one server removal marker, the
exact client acknowledgement, and one 36-byte request/event sidecar written
only after the complete census seals.

The M74 parser requires every record to keep renderer/list counters zero,
visible chunks positive, state `0x1010`, and mask `0xffff`. Identity calls are
sixteen before the transition index and fifteen from that index onward. The
M78 parser requires calls `16/16/2` then `15/15/2`, queued membership matching
those phases, two cached pages/page calls, zero fallback, and exactly one
rebuild at the transition record.

Artifact lengths, SHA-256 markers, EOF, record count, elapsed time, plan, and
nonce are cross-checked. Request latency and all timings are dynamic.
Diagnostic mode runs one replica but cannot qualify or produce release
evidence.

The frozen semantic trace reproduces SHA-256
`3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a`.
