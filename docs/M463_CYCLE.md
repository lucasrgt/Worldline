# M463 qualification cycle

`SwordHurtSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`,
retargets the saved MobSpawner `EntityId` values to `Zombie` and
`Skeleton`, sets night, and Packet7-attacks Packet24 types `54` and `51`
once each with diamond sword `276` selected. Packet38 status 2 HURT is required. `peekDeath`
stays null. The session stops after hurt and does not require death or
drops. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client implementation. There is no GUI and
no Aero path.

The frozen signal must name type `54`, type `51`, Packet38 status 2,
sword `276`, and `death=none`. It must not name Packet38 status 3,
Packet21, or Packet60.

Run directly with:

```text
java tools/smoke/SwordHurtSetCycle.java m463-sword-hurt-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`34f99909ebaad48c9c513f7aef51ee8586e82fb1b0db74e616104c22b7bb738c`.
