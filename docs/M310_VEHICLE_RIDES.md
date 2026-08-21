# M310 vehicle rides

M310 opens the official compound vehicle-mount boundary. It reuses the M154
Packet23 type-`1` boat spawn from item `333` on still water `4:60:4:9:0` and
the M155 Packet23 type-`10` minecart spawn from item `328` on rail `66` at
`4:72:5`. Packet39 attach is decoded on that same object tracker. Empty-hand
Packet7 mounts VehSee310 on the boat and VehRides310 on the cart. Each rider
freezes Packet39 attach of that player onto the shared vehicle identity.

Frozen semantic SHA-256:
`e9490bd2395a9a0e2f23738cb8956250a2a8738d5f0d1c62c27d254b43a8ff3f`.

This milestone does not claim paddle control, cart motion, powered or
detector rails, chest or furnace carts, dismount, derail, collision, or
persistence of either ride across restart.
