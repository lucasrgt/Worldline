<!-- worldline-map-schema=1 -->
<!-- boundary=inventory-session -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3b27d76f04b4e55d0c3197a091a0b98b39a0f9a5fdeee3b34b92f725e91e2472 -->

# M53 Held Block Placement

The actor acquires one qualified stone and settles downward until the server
corrects collision. The block immediately below that authoritative pose becomes
the support; the actor rises three blocks to free its former air, water, or snow-layer
cell. Placement derives its exact
Packet15 stack from the observed selected inventory slot; callers provide only
the support position and neutral block face.

Both clients independently retain the replaceable target snapshot, receive the authoritative
Packet53 stone update, and preserve the original snapshots unchanged. Packet103
empties the actor slot, Packet5 empties the peer hand, and clean player NBT
contains zero inventory entries.

This cycle does not claim arbitrary item use, block-break replacement, container
activation, placement durability across restart, server tick control, or
server-memory inspection.

Frozen expected signature SHA-256: `3b27d76f04b4e55d0c3197a091a0b98b39a0f9a5fdeee3b34b92f725e91e2472`
