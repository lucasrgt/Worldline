# M407 qualification cycle

`ChickenEggSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places default spawner `52`, retargets the
saved MobSpawner `EntityId` to `Chicken`, and awaits Packet24 type `93`.
Egg item `344` is then observed as bounded Packet21 and/or thrown Packet23
type `62` in that same session. Username `ChickEgg407` is at most 16
characters. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/ChickenEggSetCycle.java m407-chicken-egg-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`a27d5e84d4fc5e08292a9a78c2ebccf8027e9441118ed789ef3adc30d8ff97a6`.
