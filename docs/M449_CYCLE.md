# M449 qualification cycle

`WolfAngerSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Wolf`, waits for Packet24 type `95`,
and uses diamond sword `276` with Packet7 button 1. Bone item `352` is
absent. `takeTame` must stay `-1` (Packet38 is not status `6`/`7`). Packet8
health loss then proves the wild wolf turned hostile. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/WolfAngerSetCycle.java m449-wolf-anger-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`d3cb895515a26cc4e6b85659bf48537d65e2e91093f07bc035bf0ceffb3d2711`.
