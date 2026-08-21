# M517-SW item despawn age

M517-SW closes the dropped-item lifetime boundary against the official Beta 1.7.3 server JAR. An item seeded at age 5998 remains present at 5999 and is marked dead and removed at 6000. A young item remains live, while collection removes an item without reaching the expiry boundary.

This milestone owns the entity-age rule. M52 already owns Packet21 identity, Packet22 collection, and Packet29 terminal removal on the multiplayer wire.
