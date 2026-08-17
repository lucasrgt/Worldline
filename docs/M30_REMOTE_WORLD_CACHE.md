# M30 Remote World Cache

M30 adds immutable `RemoteWorldView` and the opt-in
`CachedRemoteWorldMultiplayerSession`. A view contains at most 256 aligned full
chunks, sorts them by chunk coordinates, rejects duplicates, and supports
negative-safe chunk and world block lookup without exposing protocol types.

The b1.7.3 play pump now owns one adapter-private lifecycle cache. Native
`Packet50PreChunk(load=true)` reserves a chunk coordinate, `Packet51` may fill
only a reserved coordinate, and `Packet50PreChunk(load=false)` removes both the
reservation and decoded data. Pose prelude, chat waits, chunk waits, and remote
world collection all use the same pump, so qualified lifecycle events are not
discarded when another feature is waiting for its packet.

Partial Packet51 regions are consumed but intentionally not cached; applying
their incremental block ranges is the next milestone. Sustaining a continuous
native client tick/movement stream is also outside this cache milestone.

A deterministic lifecycle oracle proves two decoded chunks, selective unload,
rejection after unload, and the 256-region bound. Two fresh official servers
then supply a decoded, lifecycle-qualified chunk apiece and exercise neutral
world-to-local block addressing.

## Non-claims

M30 does not decode block-change packets, entities, tile entities, inventory,
weather, or sounds; it does not render terrain, reproduce the native client
world, guarantee identical spawn chunks, or externally step the server tick.
