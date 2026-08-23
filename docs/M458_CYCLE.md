# M458 qualification cycle

`SlimeTouchSetCycle` rebuilds one official slime-chunk spawner in two fresh
official server JVMs. Each run enables monsters, places spawner `52` below
`y=16` in chunk `-2,-2`, retargets `EntityId` to `Slime`, and walks into
Packet24 type `55` AABB contact. Packet38 status 2 must precede Packet8
health drop. Metadata index 16 must show both size-1 and larger slimes as
one family. Split children stay outside the hash. One official EOF is
retried after a 5 second sleep.

The initial spawner placement is a four-attempt bounded operation and accepts
only an authoritative remote `52:0` state; an absent placement fails closed.

Run directly with:

```text
java tools/smoke/SlimeTouchSetCycle.java m458-slime-touch-set
```

The frozen semantic SHA-256 is
`84cc0baf6465c46adf5437018728a84b237f8d611fa461bcc6335932432f2d26`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
