# M90 qualification cycle

`LargerPageSiblingRecoveryCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

After retained record 300, the client requests removal of exact index two and
requires its server ACK, air block, and first fifteen-member batched rebuild
record. At least thirty retained records later it requests restoration and
requires the exact block entity/derived nonce, ACK, and first sixteen-member
rebuild record. The 52-byte sidecar binds both transitions to plan and nonce;
the runner reparses every M74/M78 record plus exact EOF/hashes, server marker
order, clean disconnect/stop, provenance, and clean worktrees.

Diagnostic mode cannot qualify or emit release evidence. The canonical
two-replica semantic SHA-256 is
`aac17bb2f371a10cf09b7350c228e000700ac36270dc6d3535e3de74a132a402`.
