# M55 Accepted Personal Transaction

M55 adds exact left-click take/place transitions for personal inventory storage
slots 9 through 44. The adapter requires window 0 with 45 slots and an observed
Packet103 cursor sentinel. It stages an immutable prediction, sends Packet102,
and publishes the transition only after a Packet106 true acknowledgement with
the exact window and action ID.

The accepted path commits without relying on Packet103 slot or cursor updates.
The first qualified click takes one stone from slot 36: action 1 and
predicted return stone. The second returns it: action 2 and predicted return
empty (`-1`). The committed sequence is slot `stone/empty/stone` and cursor
`empty/stone/empty`. This milestone does not claim a count of absent corrections.

An independent peer observes the actor's held item as stone, empty, then stone,
proving the official server executed both accepted clicks. Clean player NBT
retains one inventory entry.

## Boundaries

M55 does not expose right or shift click, merging, splitting, rejected
transaction recovery, chest writes, cursor persistence, crafting, or armor.
