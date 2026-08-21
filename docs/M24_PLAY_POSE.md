# M24 Multiplayer Play Pose

M24 extends `MultiplayerSession` with the explicit
`PlayableMultiplayerSession` boundary and immutable `PlayerPose`. The original
b1.7.3 wire adapter now consumes a bounded initial server prelude, including
spawn, time, weather, pre-chunk, and map-chunk packets, then decodes the native
`Packet13PlayerLookMove` position.

The adapter acknowledges that position using Beta 1.7.3's feet/stance ordering
and sends a deliberate `Packet12PlayerLook`. Two fresh official servers observe
the client, save it after disconnect, and original Worldline gzip/NBT code
requires the acknowledged position plus yaw `135.0` and pitch `-22.5` in each
player file.

Unknown prelude packet IDs, invalid lengths, repeated synchronization, and
look requests before synchronization fail closed.

## Non-claims

M24 does not claim collision-qualified movement, continuous packet pumping,
chunk interpretation, graphical rendering, multiplayer determinism, or
external server-tick control. Spawn coordinates remain observational.
