# M311 qualification cycle

`StorageCartsCycle` rebuilds the raised rail-pair fixture in two fresh
official server JVMs. Each run places rail `66` and chest-minecart item
`342`, observes Packet23 type `11`, Packet7-uses that entity, proves
Packet100 `Minecart` / 27 / 63, and closes clean. It then places a second
isolated rail, spawns furnace-minecart item `343` as Packet23 type `12`,
and Packet7-uses that entity proving no window. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`820eecba37b12ebcd44e719255868981552e3ef995e2ba92c4df32973218a71b`.

Run directly with:

```text
java tools/smoke/StorageCartsCycle.java m311-storage-carts
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
