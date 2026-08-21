# M256 chest minecart

M256 opens the official chest-minecart object boundary. Packet15 of
chest-minecart item `342` on rail `66` creates one storage EntityMinecart.
Two connected clients receive the identical Packet23 type-`11` spawn with
thrower `0` and a fixed-point pose at the rail center (`x+0.5`, `y+0.85`
floored through `*32`). This is distinct from M155 regular minecart type
`10` / item `328`.

Frozen semantic SHA-256:
`77d7cc9f33cf75c87ba161f4e0b38376562e8c3a4a1bed0d9a78aaca8f9d0a74`.

This milestone does not claim furnace carts, riding, derail, collision,
chest-window inventory, or persistence of the cart across restart.
