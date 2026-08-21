# M409 qualification cycle

`SpiderStringSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Spider`, sets night, kills Packet24
type `52`, requires Packet21 string `287`, then Packet15-places cobweb
`30`. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SpiderStringSetCycle.java m409-spider-string-set
```

The frozen semantic SHA-256 is
`625db8d839633b99daf0e73b098ad644d6f23e3d9ed4dda4f187687c1fe26fc4`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
