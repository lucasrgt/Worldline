# M64 Workbench Output

M64 converts the M63 ACK-correlated recipe model into an official behavioral
oracle. It remains in the same open workbench epoch after preparation, so the
next exact Packet102 actions are 5 and 6.

Action 5 left-clicks result slot 0 and predicts wooden slabs `44x3:2`. The
official server accepts only if its simulated returned stack exactly matches
that ID, count, and damage. `SlotCrafting` then emits Packet200 statistic ID
16842796 with increment 3 before Packet106 true and consumes one plank from each
of matrix slots 1, 2, and 3. Worldline publishes the consumed empty owned view
and slabs cursor only after both the exact statistic and ACK are observed.

Action 6 left-clicks combined slot 37 with null prediction, storing the slabs
in canonical personal slot 36 and emptying the cursor. With result and matrix
now empty, the M62 close guard permits M58's Packet101 plus accepted window-0
proof. A clean restart observes exact `44x3:2` in personal slot 36 and reopens
the persisted workbench with owned slots 0-9 empty.

## Boundaries

M64 claims this exact three-plank-to-three-wooden-slabs output, crafted-stat
count, ingredient consumption, storage, safe close, and persistence. It does not
claim generic recipes, merges, arbitrary output sizes, shift/right output
clicks, container items, rejected container recovery, XP, or achievements.
