# M456 behavior map

Official server symbols:

- `net.minecraft.src.EntityList` maps `Creeper` to Packet24 type `50`.
- Default mob spawner `52` writes NBT `EntityId=Pig`. After a clean save,
  `B173SpawnerSeed.entity` rewrites that tag to `Creeper` without `/summon`.
- Dedicated-server `spawn-monsters=false` stores world difficulty `0`, which
  despawns `EntityMob` on the next tick. M456 boots with `spawn-monsters=true`.
- `EntityMob.getCanSpawnHere` needs night (`time set 14000`) on the raised pad.
- `EntityCreeper.attackEntity` ignites when a player is within `3` by writing
  DataWatcher index `16` to `1` (protocol-14 Packet40). The fuse is `30` ticks.
- After ignition the continue range is `7`. Leaving that range writes index
  `16` to `-1` and decrements the fuse, so Packet60 strength `3` never fires.
- Headless leave uses Packet13 steps capped at `9` blocks.

This map does not claim M391 Packet60 wool/dirt craters, M448 fuse-then-explode
if the actor stays, M421 gunpowder `289`, charged creepers, or TNT strength `4`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52|cause=nbt-entityid-creeper+time-14000+approach-then-leave-cap9|wire=packet24-type50+packet40-index16-1-then--1+no-packet60|oracle=creeper-fuse-cancel-set-not-explode3-not-gunpowder289|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Creeper,mob=type50,night=14000,fuse=packet40-16:1,cancel=packet40-16:-1,packet60=absent,wait=45,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b0006bb940528fa914ae436cfe7b3ae4b73e26a997596d9275fb9c851da2e1fc`.
