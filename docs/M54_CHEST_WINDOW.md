# M54 Chest Window

M54 adds one bounded container read: activate a placed single chest with an
empty selected hand and return an immutable descriptor paired with the matching
full-window inventory. `RemoteWindowDescriptor` exposes the server-authored
window ID, neutral `CHEST` kind, title, and container-owned slot count;
`RemoteContainerWindow` binds it to the correlated `RemoteInventoryView`.

Protocol 14's Packet100 is exceptional: its title uses Java modified UTF through
`readUTF`, not the protocol's usual UTF-16 character string codec. The adapter
strictly accepts official single-chest metadata (type 0, title `Chest`, 27 owned
slots, ID 1 through 100), then requires a matching Packet104 with 63 total slots.
The total consists of 27 chest slots followed by 36 player inventory slots.

The smoke places chest block 54 through M53, confirms its server-authored state
in two remote caches, activates it with Packet15 carrying the exact empty-stack
sentinel, and observes all 63 slots empty in two fresh official-server worlds.

## Boundaries

M54 does not expose window close, clicks, transactions, cursor state, item
transfer, double chests, crafting, furnaces, or container persistence.
