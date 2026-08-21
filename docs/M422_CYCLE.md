# M422 qualification cycle

`SkeletonBoneSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Skeleton`, and reloads the actor onto
the platform. It mills bone `352` to bone meal `351x3:15` and Packet15-applies
that meal to wheat `59:0→59:7` before midnight, then kills Packet24 type
`51` for Packet21 bone `352`. Hoe and wheat stay off the skeleton archery
window. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SkeletonBoneSetCycle.java m422-skeleton-bone-set
```

The frozen semantic SHA-256 is
`c68f7f0903a8483cfac08ca4d91735085c70835d9f5e485f8055423a3ebc6dc4`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
