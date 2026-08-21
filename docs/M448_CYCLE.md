# M448 qualification cycle

`CreeperFuseSetCycle` rebuilds the raised dirt-and-wool pad in two
fresh official server JVMs. Each run retargets one spawner `EntityId` to
`Creeper`, sets night, observes Packet24 type `50`, stays inside the
vanilla proximity fuse with a movement cap of `9`, records Packet40
ignited state `1`, and requires Packet60 to follow. The signal must
name type `50`, proximity-stay plus Packet40, and fuse-then-packet60.
It must not hash M391 crater cells, strength `3`, or M421 gunpowder.
One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/CreeperFuseSetCycle.java m448-creeper-fuse-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505`.
