# M161 snowball throw

M161 opens Worldline's official object-spawn layer. It adds immutable
`RemoteObjectSpawn` evidence and a cumulative `ObjectObservationSession` API.
The server adapter decodes bounded Packet23 identity, type, fixed-point pose
and the official thrower integer, then optional velocity shorts only when that
thrower id is positive.

The official smoke climbs a raised stone column and throws snowball item `332`
with Packet15 direction `255`. Two simultaneous clients receive the identical
type-`61` Packet23 with one shared positive non-player entity ID, thrower zero
and a pose beside the actor. A production-path 21-byte fixture independently
freezes the JAR `ls` / `be` field widths.

M161 does not claim snowball collision, damage, break-on-hit, inventory
decrement hashing, Packet28 velocity, eggs, arrows, fishing hooks or object
persistence across restart.
