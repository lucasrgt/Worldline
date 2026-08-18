# M89 qualification cycle

`SiblingCellMembershipRecoveryCycle` runs two fresh graphical-client/modded-
server replicas with pinned Aero, shared plan/nonce, strict camera, and an
Aero-free common/server closure.

After retained record 300, the client requests removal of exact index four and
requires its server ACK, air block, and first fifteen-member fallback record.
At least thirty retained records later it requests restoration and requires the
exact block entity/derived nonce, ACK, and first sixteen-member rebuild record.
The 52-byte sidecar binds both transitions to plan and nonce; the runner
reparses every M74/M78 record plus exact EOF/hashes, server marker order, clean
disconnect/stop, provenance, and clean worktrees.

Diagnostic mode cannot qualify or emit release evidence. The canonical
two-replica semantic SHA-256 is
`87fa014b6cd31a48c7cffa7f839d0b407ecf823d815a80f1a578afa00828c649`.
