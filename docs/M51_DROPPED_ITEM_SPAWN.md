# M51 Dropped Item Spawn

M51 exposes the server-authoritative item entity created by the M50 drop action.
An independent observer consumes Packet21 and receives an immutable
`RemoteDroppedItem`: entity ID, exact legacy item stack, coordinates decoded
from fixed-point integers, and velocity decoded from signed bytes.

The actor acquires one qualified stone and the observer first confirms it as
the named carried item. After Packet14 status 4, the observer awaits the
matching Packet21 stone stack. Its spawn must be near the actor's last
server-authoritative pose and its bounded launch velocity must be non-zero.
Packet103 and Packet5 still prove both live hand views empty; clean player NBT
independently confirms zero persisted inventory entries.

Malformed item IDs, counts, damage, entity IDs, coordinates, and velocities
fail closed. The tracker retains only the latest qualified spawn and does not
become a general entity registry.

## Boundaries

M51 does not expose item collection, Packet22 collector correlation, Packet29
terminal removal, deterministic throw trajectories, arbitrary entity tracking,
container transactions, server tick control, or server-memory inspection.
