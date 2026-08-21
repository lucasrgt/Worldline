# M271 gold chestplate

M271 clones the M65 leather chestplate click onto gold. Actor
`GoldChest271` starts with undamaged gold chestplate `315` in personal
storage, left-clicks it into empty window-0 armor slot 6, and a named
peer observes Packet5 slot 3 as `315`.

The equipped stack is distinct from leather chestplate `299` and iron
chestplate `307`. Count one is taken from the actor window; Packet5 does
not carry count. Save plus a fresh login restores the local Packet104
chest slot and the peer Packet5 bootstrap value.

The frozen semantic SHA-256 is
`c494170f3fb2b9f4b9ec515518081834f5fe6ccd52977a7bb7e82fc946101fea`.

This milestone does not claim other gold pieces, leather or iron armor,
damaged armor, replacement, removal, durability, or combat.
