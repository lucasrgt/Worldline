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
- West dirt `3` and east wool `35` are low-resistance cells. Packet60 must list
  at least one base-layer dirt cell and one base-layer wool cell as destroyed;
  a fresh login must retain a nonempty crater. Exact ray-selected cells are not
  frozen because the official explosion ray sampler is variable.

This map does not claim charged creepers, gunpowder drops, exact ray counts,
player death, TNT fuse `46`, or Nether-bed strength `5`.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-dirt+wool-pad+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-fuse|wire=packet24-type50+packet60-strength3|oracle=creeper-explode-wool+dirt-set-not-tnt4-not-bed5|column=17,support=4:71:4:1:0,pad=3:71:4+5:71:4,destroyed-materials=dirt3+wool35,spawner=4:72:3:52:0,mob=type50,packet60=strength3,destroyed=multiple+wool+dirt,night=14000,persisted=crater,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`14ad8cdcf99568672d696cd1c79210ab82f31f2bb6bbda7f005f4c162d76f60c`.
