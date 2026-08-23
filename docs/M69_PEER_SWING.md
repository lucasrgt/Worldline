# M69 Named Peer Swing

M69 qualifies one isolated held-item swing request and one named peer
observation over the official Beta 1.7.3 server. It adds the Packet18 seam
needed to shape a later real-client combat/render experiment without changing
the frozen M66 Packet7-only attack contract.

## Contract

`PeerSwingSession.swingHeldItem()` requires a synchronized pose, no active
container, an observed empty cursor, and an exact selected diamond sword
`276x1:0`. It emits Packet18 with the authenticated client's local entity ID and
animation code 1. `awaitPeerSwing(username)` arms a fresh expectation, resolves
the username through Packet20 identity state, and accepts only a matching
Packet18 entity ID with animation 1.

`RemoteSwingRequest` records the local request identity. `RemotePeerSwing`
records the independent named peer observation. Neither value invents a target,
damage, health, or server acknowledgment.

## Evidence and non-claims

A production-path byte fixture freezes Packet18 as packet ID 18, a four-byte
entity ID, and animation byte 1. Two fresh official-server scenarios establish
Packet20 identity and Packet5 sword state before one request, then observe the
same named entity and animation on the peer. The actor's sword persists as one
inventory entry after clean disconnect and save.

Both identities are seeded at fixed spawn-relative positions through official
player NBT, with sword `276` already in actor slot 36. No dropped-item pickup or
random first-login placement participates in the evidence.

Packet18 is not an attack acknowledgment. The server authenticates the socket
and does not rely on the claimed entity ID. M69 does not claim a target,
Packet7/38/8, damage, health, rendering, Aero timing, repeated swings,
reconnect identity cleanup, or causality beyond the isolated request/observation
scenario.
