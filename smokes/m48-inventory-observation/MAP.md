# M48 Server Inventory Observation

Two fresh unmodified Beta 1.7.3 dedicated servers send the initial 45-slot
player window through Packet104. Each inventory starts empty. An authorized
vanilla `give` command creates a real item entity; after its pickup delay, the
server inserts one stone stack and sends the matching Packet103 delta for
player-window slot 36.

The subject uses only `InventoryMultiplayerSession` and neutral immutable item,
slot, and window types after adapter construction. The adapter never reads
server memory or world files for the live observation. The persisted player
NBT is checked only after clean disconnect and save as independent evidence.

This cycle does not claim held-slot selection, crafting/container mutation,
arbitrary item metadata coverage, server tick control, or inventory writes.
