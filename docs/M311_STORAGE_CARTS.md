# M311 storage carts

M311 freezes the official storage-cart set: chest-minecart window open and
furnace-minecart presence plus interact. Packet15 of chest-minecart item
`342` on rail `66` creates one storage EntityMinecart. The actor then sends
Packet7 interact (`leftClick=0`) at that Packet23 type-`11` identity. The
official server answers with Packet100 type `0` titled `Minecart` and 27
owned slots, paired with Packet104 of 63 combined slots.

A second isolated rail then receives furnace-minecart item `343`. That
emits Packet23 type `12` through the existing object tracker. Packet7
interact against the type-`12` identity is the furnace-cart interact
oracle: vanilla opens no Packet100 window. Fuel, push, and powered-rail
motion are not this freeze.

This is not Packet15 direction-255 air-use, not M256/M257 spawn-only, and
not a block-chest window titled `Chest`. Close uses the existing M58
Packet101 plus personal-window proof. Headless `B173WireClient` only. No
GUI. No Aero. No second Packet23 tracker.

Frozen semantic SHA-256:
`820eecba37b12ebcd44e719255868981552e3ef995e2ba92c4df32973218a71b`.

This milestone does not claim item transfer, riding, derail, furnace-cart
fuel, or persistence of cart contents across restart.
