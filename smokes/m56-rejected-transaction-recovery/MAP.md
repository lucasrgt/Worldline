# M56 Rejected Transaction Recovery

An adapter-private qualification probe performs the normal personal-window
left-take simulation but deliberately encodes stale predicted empty. Callers
cannot supply predictions. The official server rejects action 1 after moving
the stone to its cursor.

Worldline immediately sends the vanilla Packet106 true re-enable ACK, then
requires the ordered Packet104 window-0 full resync and Packet103 cursor stone.
Only after both does it publish immutable recovery. A correct action 2 restores
the stone and receives Packet106 true, proving transactions were re-enabled.

The stone fixture is written directly into the official player NBT at personal
slot 36 before login, and both clients receive fixed nearby NBT positions. This
removes `/give` drop placement, pickup timing, and random spawn distance from
the transaction-recovery qualification without changing its wire path.

This cycle does not expose prediction forgery, rejected chest clicks, duplicate
ACK handling, right/shift click, crafting, or generic conflict resolution.
