# M155 minecart spawn

M155 opens the official vehicle-object boundary. Packet15 of minecart item
`328` on rail `66` creates one EntityMinecart. Two connected clients receive
the identical Packet23 type-`10` spawn with thrower `0` and a fixed-point pose
at the rail center (`x+0.5`, `y+0.85` floored through `*32`).

Frozen semantic SHA-256:
`8bbf2ce26b50b36cdb15763b126864882c8e138b89113c1fe6dcd75988703fab`.

This milestone does not claim powered-rail motion, detector or powered rails,
chest or furnace carts, riding, derail, collision, or persistence of the cart
across restart.
