# M53 Held Block Placement

M53 adds one bounded selected-item action: place the observed held block against
a neutral support position and `BlockFace`. The adapter re-sends Packet16, then
encodes Packet15 with the exact legacy ID, count, and damage from window 0 slot
`36 + selectedHotbarSlot`. Callers cannot provide or forge the wire stack.

The actor settles to an authoritative collision pose, freezes the block below
its feet as support, and rises three blocks to free its former replaceable
target: air, flowing or still water, or a snow layer. The early independent observer retains that same
baseline. After placing
one qualified stone against the upper face, both clients require the exact
Packet53 `BlockState[1:0]` and retain their original snapshots unchanged.
Packet103 empties the actor slot, Packet5 empties the named peer hand, and clean
player NBT independently contains zero inventory entries.

The outbound helper rejects null positions/faces, out-of-height targets,
non-player inventory windows, empty selected slots, and item IDs outside the
legacy block range. The official server remains authoritative for acceptance.

## Boundaries

M53 does not expose caller-supplied placement stacks, arbitrary item use,
container activation, block-break replacement, placement durability across
restart, server tick control, or server-memory inspection.
