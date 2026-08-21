# M85 qualification cycle

`NaturalMembershipRecoveryCycle` builds pinned Aero in a disposable worktree,
verifies the common/server closure has no Aero or client imports, and runs two
fresh graphical-client/modded-server replicas with an identical plan and root
nonce.

Each client opens a complete M74/M78 bracket, sends one typed phase-one removal
request after retained record 300, waits for the first fifteen-member record,
then sends one typed phase-two restoration request after thirty more retained
records. The server validates both transitions and emits distinct removal,
restoration-state, ACK, and lifecycle evidence.

The 52-byte sidecar binds schema, nonce, plan, both request/event pairs, and the
two event rebuild counts. The runner reparses it together with every M74 and
M78 record. It requires membership `16 -> 15 -> 16`, page calls `4 -> 3 -> 4`,
direct fallback and render/list calls `0 -> 1 -> 0`, cache count four, and one
rebuild only on restoration. Artifact lengths, EOF, hashes, fixed camera,
server marker order, clean disconnect/stop, exact Aero provenance, and clean
worktrees are fail-closed.

Diagnostic mode runs one replica but cannot qualify or write release evidence.
The canonical two-replica run reproduced SHA-256
`6afe38b10186f67d95eef5d1a1beca81bd168417d7d32d3579dfd654aae0445b`.
