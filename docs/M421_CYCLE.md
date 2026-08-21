# M421 qualification cycle

`CreeperGunpowderSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places workbench `58` and one default
spawner `52`, retargets the saved MobSpawner `EntityId` to `Creeper`, sets
night, kills Packet24 type `50` from outside the fuse, requires Packet21
gunpowder `289`, then Packet102-crafts TNT `46` from `289` plus sand `12`.
One official EOF is retried after a 5 second sleep.

The frozen signal must name type `50`, Packet21 `289`, TNT craft `46`, and
workbench `58`. It must not name Packet60.

Run directly with:

```text
java tools/smoke/CreeperGunpowderSetCycle.java m421-creeper-gunpowder-set
```

The frozen semantic SHA-256 is
`f01c7a65ddde0ddb0cd8f27f6e1c76e896f866c0bf9cc6f8af973bd1def648dc`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
