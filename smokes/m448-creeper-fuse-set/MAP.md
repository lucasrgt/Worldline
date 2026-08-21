# M448 behavior map

Official server symbols:

- `net.minecraft.src.EntityList` maps `Creeper` to Packet24 type `50`.
- Default mob spawner `52` writes NBT `EntityId=Pig`. After a clean save,
  `B173SpawnerSeed.entity` rewrites that tag to `Creeper` without `/summon`.
- Dedicated-server `spawn-monsters=false` stores world difficulty `0`, which
  despawns `EntityMob` on the next tick. M448 boots with `spawn-monsters=true`.
- `EntityMob.getCanSpawnHere` needs night (`time set 14000`) on the raised pad.
- `EntityCreeper` ignites when a player is within `3`. DataWatcher index `16`
  becomes `1` as protocol-14 Packet40. The actor stays with movement cap `9`.
- If the player stays, Packet60 follows. Exact fuse tick length is not hashed.
  M391 hashes the wool-plus-dirt crater; M448 hashes fuse-then-explode order.

This map does not claim charged creepers, gunpowder drops, crater cells,
player death, TNT fuse `46`, or fuse-cancel-on-leave.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-dirt+wool-pad+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-stay|wire=packet24-type50+packet40-state1+packet60|oracle=creeper-fuse-then-packet60-not-crater-not-drop289-not-leave|column=17,support=4:71:4:1:0,pad=3:71:4+5:71:4,spawner=4:72:3:52:0,mob=type50,fuse=proximity-stay+packet40-state1,order=fuse-then-packet60,packet60=followed,stay=true,night=14000,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505`.
