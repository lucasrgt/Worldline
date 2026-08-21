# M157 bow arrow

M157 opens the official projectile-object boundary. A player holding bow `261`
with at least one arrow `262` sends Packet15 with coordinates `(-1, 255, -1)`
and direction `255`. `NetServerHandler.handlePlace` calls
`ItemInWorldManager.func_6154_a`, which invokes `ItemBow.onItemRightClick`.
The server consumes one arrow and joins an `EntityArrow` whose tracker spawn
packet is Packet23 type 60. The thrower field is the shooter's entity id.
Official extras (three motion shorts) are present only when that id is greater
than zero, so entity id 0 omits them. The frozen hash therefore keeps the
shared type-60 identity and thrower match rather than raw extras.

This milestone does not claim hit damage, stuck-arrow pickup, inventory
decrement hashing, snowballs, eggs, fishing floats, TNT/falling-sand object
types, or persistence of the flying or in-ground arrow across restart.
