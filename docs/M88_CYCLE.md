# M88 qualification cycle

`ReverseTwoCellMembershipRecoveryCycle` runs two fresh graphical-client/modded-
server replicas with pinned Aero, shared plan/nonce, strict camera, and an
Aero-free common/server closure.

The client performs remove/restore for index one, then remove/restore for index
zero, spacing every request by at least thirty retained records after the prior
transition. A 76-byte sidecar binds four `{request,event,index}` triples to plan
and nonce. The runner reparses every M74/M78 record and requires exact
rebuild-first/fallback-second topology, exact EOF/hashes, server marker order,
clean disconnect/stop, provenance, and clean worktrees.

Diagnostic mode cannot qualify or emit release evidence. The canonical
two-replica semantic SHA-256 is
`986d67c17068113e152c7cec8614bbc518629fff4c27619ec488da6c2548c079`.
