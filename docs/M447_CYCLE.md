# M447 qualification cycle

`SpiderClimbSetCycle` rebuilds the raised grass platform plus cobble `4`
and oak plank `5` walls in two fresh official server JVMs. Each run places
one default spawner `52`, retargets the saved MobSpawner `EntityId` to
`Spider`, sets night, and observes Packet24 type `52` Packet31/33/34
positive-Y motion on both materials in one session. A 24-fence perimeter,
arena-contained spawn selection, inward walls, and bounded non-blocking movement
polls prevent off-platform or vanished-entity waits. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/SpiderClimbSetCycle.java m447-spider-climb-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`dd9ff5fff66e6f0e70d4f4dd873aa178184ec6e7b407e99cc861cec68916f588`.
