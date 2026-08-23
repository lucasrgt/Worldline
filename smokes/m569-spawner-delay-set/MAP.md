<!-- worldline-map-schema=1 -->
<!-- boundary=m569-spawner-delay-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19 -->

# M569 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is
rewritten from `Pig` to `Zombie` and `Delay` is rewritten to `1`. Console
time `14000` makes the platform dark enough for `EntityMob.getCanSpawnHere`.

The actor logs in 24 blocks above the spawner, outside the official 16-block
activation range. Packet24 type `54` stays absent for a 40-tick wait. The
actor then stations beside the spawner; Packet24 type `54` must appear
inside the spawner's `4×1×4` volume.

This map does not claim pig identity (M141), creeper+spider identity (M390),
or spawn-light suppression (M564). Headless `B173WireClient` protocol-14
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52|cause=nbt-entityid-zombie+nbt-delay-1+time-14000+range-24-above-then-near|wire=packet24-type54-absent-far+packet24-type54-near|oracle=spawner-delay-range-not-pig-not-creeper-spider-not-light|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Zombie,delay=1,far=24,wait=40,absent=true,near=type54,night=14000,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.
