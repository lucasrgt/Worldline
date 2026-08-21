# M469 void death set

M469 is a compound official void-death SET. One empty `VoidDeath469`
starts in underside void air above the kill plane. Packet13 walks down
in steps of at most 9 until pose `y` is below 0 and then below `-64`.
Packet8 health reaches `0`. Packet9 then returns the actor to overworld
dimension `0` at health `20`, which persists after a clean save.

This is distinct from M135, which seeds already under the kill plane and
waits. It does not claim M135 player respawn from mob or lava, M461 fall
damage above the void, or M465 environmental death. Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.
