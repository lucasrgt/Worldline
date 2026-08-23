# M391 behavior map

Official server symbols:

- `net.minecraft.src.EntityList` maps `Creeper` to Packet24 type `50`.
- Default mob spawner `52` writes NBT `EntityId=Pig`. After a clean save,
  `B173SpawnerSeed.entity` rewrites that tag to `Creeper` without `/summon`.
- Dedicated-server `spawn-monsters=false` stores world difficulty `0`, which
  despawns `EntityMob` on the next tick. M391 boots with `spawn-monsters=true`.
- `EntityMob.getCanSpawnHere` needs night (`time set 14000`) on the raised pad.
- `EntityCreeper` ignites when a player is within `3` and then calls
  `World.newExplosion(this, x, y, z, 3F)` — protocol-14 Packet60 strength
  `3`. TNT (M137) is strength `4`. Nether beds (M359) are strength `5`.
- A `7×7` checkerboard alternates dirt `3` and wool `35` around the center.
  The oracle selects one Packet60-destroyed cell of each known fixture material,
  proves both are live air, then reloads those exact coordinates as air.

This map does not claim charged creepers, gunpowder drops, exact ray counts,
player death, TNT fuse `46`, or Nether-bed strength `5`.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-checkerboard-dirt+wool-pad+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-fuse|wire=packet24-type50+packet60-strength3|oracle=creeper-explode-wool+dirt-set-not-tnt4-not-bed5|column=17,support=4:71:4:1:0,pad=7x7-checkerboard-3+35,dirt=3:0->0:0,wool=35:0->0:0,spawner=4:72:3:52:0,mob=type50,packet60=strength3,destroyed=multiple+wool+dirt,night=14000,persisted=air,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`389f99f5639c66342a8560c23fe7e85cbe1aafc6e71530ed05c0cc7bbdbb19c0`.
