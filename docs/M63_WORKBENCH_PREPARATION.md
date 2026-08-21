# M63 Workbench Preparation

M63 adds one narrow write boundary to the typed M62 workbench. The caller
supplies only a personal storage slot containing exactly three oak planks
`5x3:0`. The adapter owns the active window, action counter, mouse buttons,
predictions, grid slots, cursor, and recipe model.

Action 1 left-clicks the combined personal slot and moves all three planks to
the cursor. Actions 2, 3, and 4 right-click empty matrix slots 1, 2, and 3,
placing one plank per slot while the cursor count moves 3 to 2 to 1 to empty.
Every transition commits the active 46-slot view, canonical window 0, and cursor
only after the matching Packet106 true.

A byte-level fixture exercises the same Packet102 encoder and freezes matrix
slots 1/2/3, button 1, actions 2/3/4, shift false, and null stack prediction.
The accepted null return alone cannot distinguish left from right on an empty
slot, so the behavioral smoke treats cursor and matrix contents as the local
model correlated to each ACK, not as independently transmitted state.

The adapter models the two-wide intermediate result as wooden pressure plate
`72x1:0` and the completed first row as wooden slabs `44x3:2`. Protocol 14 does
not transmit SlotCrafting changes on these accepted grid clicks, so M63 does
not claim those result values as independent wire observations. M64 must confirm
the final model by taking output with the exact slabs prediction.

Because `ContainerWorkbench` drops matrix contents on close, the public M58
close boundary rejects the prepared window before Packet101. The M63 smoke
proves that fail-closed behavior and then tears down the protocol session.

## Boundaries

M63 claims exact left-take behavior, byte-exact right-place encoding, accepted
correlation, ACK-correlated matrix/cursor modeling, local recipe modeling, and
nonempty-close rejection. It does not claim generic recipes, authoritative
result observation, output take, ingredient consumption, persistence, merges,
shift clicks, arbitrary slots, or forced close semantics.
