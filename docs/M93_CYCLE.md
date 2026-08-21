# M93 qualification cycle

`FullPageDepletionRecoveryCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

After retained record 300, the client removes exact indices
`1,2,3,5,6,7`. Each later request waits at least thirty retained records after
the preceding observed transition; restoration then proceeds `7,6,5,3,2,1`.
The runner requires exact ordinal/operation/index-bound ACK and state, air or
block-entity/derived-nonce state, and the first correlated complete record for
all twelve transitions.

Configuration, process arguments, and the runtime itself require page TTL
100000, rebuild budget eight, uncapped vanilla FPS, and disabled Aero pacing.
The 172-byte sidecar binds twelve request/event/index triples to plan and
nonce. The runner reparses every M74/M78 record plus exact EOF and hashes,
server marker order, clean disconnect/stop, provenance, and clean worktrees.
Diagnostic mode cannot qualify or emit release evidence.

The canonical two-replica semantic SHA-256 is
`f0f506ffa69950d8d4030819a4c6c5ca3f190edcfd3f4ba29f3a4ef4129959ad`.
