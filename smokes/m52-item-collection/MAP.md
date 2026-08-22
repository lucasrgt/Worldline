# M52 Named Item Collection

The observer binds the actor's Packet20 entity ID before the actor drops one
qualified stone. The exact Packet21 dropped-item entity is then followed through
Packet22 collection by that named actor and Packet29 terminal removal.

The observer moves vertically out of pickup range while staying inside entity
tracking range. After the official pickup delay, Packet103 restores the actor's
stone, Packet5 restores the peer-carried stone, and clean player NBT contains
one inventory entry.

This cycle does not claim arbitrary entity collection, item ownership, exact
pickup timing, container transactions, crafting, server tick control, or
server-memory inspection.

Frozen expected signature SHA-256: `905fe8b02bdc2f81e2280d4658b81440e4d975e6d52ff83a4fd573d0ad8f77af`
