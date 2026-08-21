# M54 Chest Window

The actor acquires and places one chest through the M53 boundary. An independent
observer confirms the final chest block before the actor activates it with an
empty selected hand.

The actor decodes the dedicated Packet100 modified-UTF descriptor and correlates
it with the matching Packet104 full view. The qualified boundary is one single
chest: type 0, title `Chest`, 27 container-owned slots, and 63 total empty slots
including the player inventory.

This cycle does not claim window close, clicks, transactions, item transfer,
double chests, crafting, furnace progress, or container persistence.
