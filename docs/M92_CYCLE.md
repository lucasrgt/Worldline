# M92 qualification cycle

`ThirdMemberDepletionRecoveryCycle` runs two fresh graphical-client/modded-
server replicas with pinned Aero, shared plan/nonce, strict camera, and an
Aero-free common/server closure.

After retained record 300, the client removes exact indices one, two, and
three. Each later request waits at least thirty retained records after the
preceding observed transition; restoration then proceeds three, two, one. The
runner requires exact ordinal/operation/index-bound ACKs and restore state,
air or block-entity/derived-nonce state, and the first correlated complete
rebuild record for all six transitions.

The 100-byte sidecar binds six request/event/index triples to plan and nonce.
The runner reparses every M74/M78 record plus exact EOF and hashes, server
marker order, clean disconnect/stop, provenance, and clean worktrees.
Diagnostic mode cannot qualify or emit release evidence.

The canonical two-replica semantic SHA-256 is
`a82e3eb16c9c12a3901e03775d53898a725914562f5bd971d7dc5d2444c75104`.
