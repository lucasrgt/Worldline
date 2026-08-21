# M406 qualification cycle

`SheepDyeSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`,
retargets the saved MobSpawner `EntityId` values to `Sheep`, dyes two
living type-`91` sheep with dyes `351:1` and `351:11`, and shears them
with item `359`. Packet21 must include wool damages `35:14` and `35:4`
with no Packet38 status 3. One official EOF is retried after a 5 second
sleep.

Run directly with:

```text
java tools/smoke/SheepDyeSetCycle.java m406-sheep-dye-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`0c2857eb2e2bf4aaa39c631eced8f47d470862396ae8e7981d41c6c0a0775cb7`.
