# M316 qualification cycle

`ShearsSetCycle` rebuilds the raised grass platform in two fresh official
server JVMs. Each run shears oak leaves `18` with item `359`, places
default spawner `52`, retargets the saved MobSpawner `EntityId` to
`Sheep`, and shears the living type-`91` sheep. Packet21 must include
leaf item `18` and wool item `35` with no Packet38 status 3. One official
EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/ShearsSetCycle.java m316-shears-set
```

The frozen semantic SHA-256 is
`91eec7f3061f3c9cb956cd25ebcc7ece6a66055262a45538081a8ad72d79426e`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
