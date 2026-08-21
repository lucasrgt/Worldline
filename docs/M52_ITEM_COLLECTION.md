# M52 Named Item Collection

M52 follows one exact M51 dropped-item entity through collection and terminal
removal. Packet22 identifies both the collected entity and the collector.
Worldline resolves the collector through the local login identity or a strict
Packet20 entity-to-username binding, then requires Packet29 for the same item
before returning an immutable `RemoteItemCollection`.

The qualification isolates the observer vertically while preserving entity
tracking. The actor drops one qualified stone, both live hand views become
empty, and the observer records the exact Packet21 entity. After the official
pickup delay, Packet22 names the actor as collector and Packet29 removes that
same item. Packet103 restores the actor's stone, Packet5 restores the peer-held
stone, and clean player NBT independently contains one inventory entry.

Item tracking is session-bounded and fails closed at 256 distinct observed
spawns. Packet29 without Packet22 is a valid non-collection terminal lifecycle;
it cannot satisfy a collection wait. Collection after destruction, duplicates,
conflicting identities, and unknown collectors fail closed.

## Boundaries

M52 does not expose item ownership, arbitrary entity collection, exact pickup
timing, container transactions, crafting, server tick control, world-scale
pathfinding, or server-memory inspection.
