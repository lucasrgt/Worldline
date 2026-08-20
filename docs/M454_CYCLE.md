# M454 qualification cycle

`PeacefulDespawnSetCycle` rebuilds a raised grass platform in two fresh
official server JVMs. Each run names both profiles: Easy
(`spawn-monsters=true`, `difficulty=1`) persists Packet24 types `50`
and `54`, then Peaceful (`difficulty=0`) keeps types among `50`, `51`,
and `54` absent or despawned. One official EOF is retried after a 5
second sleep.

Run directly with:

```text
java tools/smoke/PeacefulDespawnSetCycle.java m454-peaceful-despawn-set
```

The frozen semantic SHA-256 is
`8a4c4acadf23008e8fed2fdbc1d9c05c903c65c527c3489dabee48e7d2183abe`.

Canonical evidence uses two official server JVMs and six client
sessions. Headless protocol-14 only. No GUI. No Aero.
