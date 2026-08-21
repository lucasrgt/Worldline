# M50 Drop Held Item

M50 adds one bounded multiplayer action: drop the complete current held stack.
The b1.7.3 adapter emits Packet14 with status 4 and neutral coordinates, matching
the original protocol action. The server remains authoritative; Worldline does
not mutate the inventory or peer observation optimistically.

The actor first acquires one qualified stone in hotbar slot 0. After an
independent observer confirms that carried item, the actor drops it. Packet103
makes the actor's immutable slot 36 empty, and Packet5 makes the observer's
named `RemoteHeldItem` empty. Empty Packet20 and Packet5 sentinels are decoded
strictly; malformed negative identifiers fail closed.

Both clients disconnect before the official server saves. The actor's player
NBT then contains zero inventory entries, providing persistence evidence that
is independent of both live protocol views.

## Boundaries

M50 drops only the complete current stack. It does not expose single-item
drops, item-entity spawn or trajectory tracking, collection, click-window
transactions, crafting, container control, server tick control, or server
memory inspection.
