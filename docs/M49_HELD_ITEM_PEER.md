# M49 Held Item Peer Observation

M49 adds bounded held-hotbar selection to the multiplayer session. The caller
chooses an index from 0 through 8; the b1.7.3 adapter emits Packet16 with that
index. Selection has no optimistic local acknowledgement.

Independent sessions can await an exact `RemoteHeldItem`. The inbound tracker
first consumes Packet20 to bind the remote entity ID to a validated username.
It then accepts Packet5 equipment slot zero as the server-authoritative carried
item for that named player. Armor updates, unknown entities, empty equipment,
and unrelated slots cannot satisfy a non-empty expected item.

The qualification gives the actor one stone and one dirt through the M48 path,
proving exact immutable stacks in hotbar slots 0 and 1. Only then does the
observer connect. The actor selects slot 1 through Packet16, and the observer
receives dirt through Packet5. Both clients remain isolated; the observer never
reads the actor's inventory. Clean save independently confirms two player NBT
inventory entries.

## Boundaries

M49 does not expose equipment counts, empty-hand events, click-window actions,
drop actions, crafting, container transactions, entity registries, tick control,
or server-memory inspection.
