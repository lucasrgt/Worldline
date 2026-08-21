# M311 behavior map

The fixture raises an isolated stone column and places rail `66:0`. Using
chest-minecart item `342` on that rail emits official Packet23 type `11`.
Packet7 entity interact (leftClick `0`) against that identity opens Packet100
type `0` titled `Minecart` with 27 owned slots (63 combined). Close is M58
Packet101 plus window-0 proof.

A second isolated rail `66:0` two blocks east then receives furnace-minecart
item `343`, which emits Packet23 type `12` on the same `B173ObjectTracker`.
Packet7 interact against that identity is accepted and opens no Packet100.
Vanilla furnace carts have no inventory window; fuel/push is not this freeze.

This is distinct from M256/M257 spawn-only, M155 type `10`, and block-chest
title `Chest`. Packet15 direction 255 air-use is not this vehicle use.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-rail66-pair|cause=packet7-interact-chestminecart+packet15-furnace-minecart343|wire=packet23-type11+packet100-type0-Minecart-27+packet23-type12+packet7-nowindow|oracle=storage-carts-type11-window+type12-spawn-interact|column=17,rail=4:72:4:66:0,chest=type11+thrower0+fixed144:2331:144,window=type0+Minecart+slots27+total63,close=clean,furnaceRail=6:72:4:66:0,furnace=type12+thrower0+fixed208:2331:144,interact=packet7+nowindow,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`820eecba37b12ebcd44e719255868981552e3ef995e2ba92c4df32973218a71b`.
