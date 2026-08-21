# M87 qualification cycle

`TwoCellMembershipRecoveryCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The client performs remove/restore for index zero, then remove/restore for
index one, spacing every request by at least thirty retained records after the
prior transition. A 76-byte sidecar binds four `{request,event,index}` triples
to plan and nonce. The runner reparses every M74/M78 record and requires the
exact fallback-first/rebuild-second topology, exact EOF/hashes, server marker
order, clean disconnect/stop, provenance, and clean worktrees.

Diagnostic mode cannot qualify or emit release evidence. The canonical
two-replica semantic SHA-256 is
`091dd5a68a9e7650ef91496f86cbc9dc5e82e006863d097a8e3c637402a103a4`.
