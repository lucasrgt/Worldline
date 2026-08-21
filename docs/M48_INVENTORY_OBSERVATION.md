# M48 Server Inventory Observation

M48 adds a bounded, neutral inventory read surface to the multiplayer session.
`RemoteItemStack` represents a valid legacy item ID, count, and damage value.
`RemoteInventorySlot` makes emptiness explicit without exposing nullable list
entries. `RemoteInventoryView` owns a contiguous immutable slot collection for
one server window.

`InventoryMultiplayerSession.awaitInventory()` waits for the first complete
server window. `inventory()` returns the latest immutable view. The b1.7.3
adapter decodes Packet104 as an atomic window replacement and Packet103 as a
single-slot replacement only when both the window ID and slot index match the
current view. Cursor updates and updates for unrelated windows cannot corrupt
the observed player window.

The qualification starts two fresh official dedicated servers. Each sends the
45-slot empty player inventory. A vanilla operator command creates one stone
item above the connected player. After the official pickup delay and collision
path, the server updates slot 36 through Packet103. The client observes exactly
one `1x1:0` stack, then clean disconnect, save, and player NBT independently
confirm one persisted inventory entry.

## Boundaries

M48 is read-only. It adds no held-slot command, click-window mutation, crafting,
container transaction, arbitrary metadata matrix, server-memory access, tick
control, or inventory reconciliation protocol. Packet bounds and malformed
stack fields fail closed.
