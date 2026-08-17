# M57 Personal 2x2 Crafting

M57 composes the accepted personal-window transaction engine into one bounded
Minecraft Beta 1.7.3 recipe: a single log `17x1:0` becomes four planks `5x4:0`.
The public caller supplies only the occupied inventory slot; predictions,
crafting-grid coordinates, result, and action IDs remain adapter-owned.

The personal window has result slot 0, 2x2 matrix slots 1-4, and inventory slots
9-44. Worldline executes four left/non-shift Packet102 actions: take the log,
place it in matrix slot 1, take the derived result, and store the planks back in
the source slot. Each staged transition commits only after its exact Packet106
true acknowledgement.

The official server suppresses ordinary Packet103 slot/cursor updates for
accepted clicks and always excludes SlotCrafting result updates. Consequently,
Worldline derives the exact grid/result transition locally. Acceptance of the
planks prediction when taking slot 0 is the official recipe oracle.

Qualification then reuses the package-private M56 stale probe. Its rejected
action forces Packet104 plus cursor Packet103 recovery, proving authoritatively
that result/grid slots 0-4 are empty and the cursor owns four planks. A final
accepted action restores the planks before persistence.

## Boundaries

M57 does not claim other recipes or metadata, caller-provided predictions,
right/shift clicks, workbench crafting, container items/remainders, or generic
container transactions.
