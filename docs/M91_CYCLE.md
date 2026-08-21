# M91 qualification cycle

`LargerPageDepletionRecoveryCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

After retained record 300, the client removes exact index one. Each later
request waits at least thirty retained records after the preceding observed
transition: remove index two, restore index two, then restore index one. The
runner requires exact ordinal/operation/index-bound ACKs and restore state,
air or block-entity/derived-nonce state, and the first correlated complete
rebuild record for each transition.

The 76-byte sidecar binds all four request/event/index triples to plan and
nonce. The runner reparses every M74/M78 record plus exact EOF and hashes,
server marker order, clean disconnect/stop, provenance, and clean worktrees.
Diagnostic mode cannot qualify or emit release evidence.

The canonical two-replica semantic SHA-256 is
`5f019eb32c7f34b31ca907e9fdbec3b827254a08cdf0cbe11a91c703644b2f7e`.
