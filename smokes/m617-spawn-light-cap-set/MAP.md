# M617 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Creeper` and `Zombie`. The unlit world is copied
before either arm runs so cover and substrate stay identical.

Console time `14000` makes the unlit pad dark enough (`block-light 0`,
effective night light `<= 7`) for `EntityMob.getCanSpawnHere`. Packet24
type `50` or type `54` must appear inside the official spawner volume.

The copied world then places four floor torches `50:5` on grass cells at
offsets `±2,±2` from the first spawner. Those torches occupy four air
cells only; the remaining pad stays grass. Sampled pad block-light must
be `>= 8`. The same night must keep nearby Packet24 types `50` and `54`
absent.

This map does not claim pig identity (M141), identity-only natural
hostiles (M435), remaining-spawner creeper+spider identity (M390),
spawner delay or activation range (M569), or M564's 49-torch carpet that
changed cover. Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-creeper+zombie+time-14000+sparse-torch-50-5-light-cap|wire=packet24-type50-or-54-dark+packet24-type50-or-54-absent-torch|oracle=spawn-light-cap-unlit-permit-torch-reject-not-m435-natural-not-m569-delay-not-m564-carpet|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Creeper+Zombie,dark=type50-or-54,torch=50:5x4,dark-light=0,lit-light>=8,torch-arm=absent,night=14000,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`7b7f1afdfd24186f8c299874e34b09f3ab9ff8edd782a7b955a91ebd8d042d20`.
