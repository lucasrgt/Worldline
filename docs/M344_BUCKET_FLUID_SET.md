# M344 bucket fluid set

M344 opens the official compound water-bucket and lava-bucket set. Water
bucket `326` places still water `9:0` into a raised four-wall stone basin
and empty bucket `325` picks that source back up. Lava bucket `327` then
places still lava `11:0` into the same empty basin from the south wall,
and empty bucket `325` picks that source back up. The frozen signal
includes place plus pickup for both fluids (`326/9` and `327/11`).

This is distinct from shipping M168 and M181 (pickup only) and from
shipping M254 and M255 (place only). It does not claim flowing fluids,
obsidian reaction, milk, or fire spread. Headless `B173WireClient` only.
No GUI. No Aero.

The frozen semantic SHA-256 is
`fe76fdf6b8ec887d8efc4caa81ce926b3efad2a42207cbefd9b6a21f9b66b789`.
