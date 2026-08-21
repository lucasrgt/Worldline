# M67 Chest Retrieval

M67 closes the single-chest lifecycle opened by M59. It retrieves one exact
stone stack from an active chest into an empty personal storage slot without
exposing raw Packet102 predictions or active window IDs.

## Contract

`ChestRetrievalSession.retrieveFromOpenChest(chestSlot, personalSlot)` requires
an active single-chest view, an observed empty cursor, exact stone `1x1:0` in
the selected owned slot, and an empty mapped personal destination. The first
accepted click moves chest0 to the cursor. The second accepted click stores it
in combined slot 54 and canonical personal slot 36.

Each Packet106 acceptance atomically commits the 63-slot combined view, the
45-slot personal view, and cursor state. `RemoteChestRetrieval` preserves the
before/after snapshots, action pair, exact stack, full tail mapping, and
unchanged unrelated slots.

## Evidence and non-claims

Two fresh scenarios each use two official server processes. The first process
places a chest, stores stone through M59, reopens it, retrieves through actions
1 and 2, closes, and saves. A new process proves chest0 empty, personal36 stone,
combined54 stone, and one player NBT inventory entry.

M67 does not claim generic item retrieval, merge behavior, split stacks,
shift/right clicks, rejected active-container recovery, concurrent chest
mutation, or remote-window automation beyond the exact single-chest boundary.
