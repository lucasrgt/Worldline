# M453 behavior map

One player is seeded at Y `-80` with hotbar stone `1`, cobble `4`, and dirt
`3`. Bounded heartbeats let vanilla void damage drive Packet8 health from
`20` to `0`. The official server then emits Packet21 for those three seeded
item ids. Drop coordinates and velocities are not frozen.

This map is the player-death plus multi-item-drop family. It does not claim
M388 zombie feather `288` / skeleton arrow `262`, M444 pig pork `319` /
sheep wool `35:0`, M135 Packet9 respawn, or XP (none in Beta 1.7.3).
Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=seeded-y-80+hotbar-stone1+cobble4+dirt3|cause=vanilla-void-damage|wire=packet8-health20->0+packet21-1+packet21-4+packet21-3|oracle=player-death-multi-item-drops-not-mob-drops|health=20->0,death=seeded-below-world+vanilla-void-damage+packet8-0,hotbar=1+4+3,drops=packet21-1+packet21-4+packet21-3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`6d7e55c8c86f1540d7306a507b0a07af3ef9cbe3b6f6c79cf2b87663beab7ed0`.
