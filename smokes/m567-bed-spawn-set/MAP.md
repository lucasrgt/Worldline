<!-- worldline-map-schema=1 -->
<!-- boundary=m567-bed-spawn-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed -->

# M567 behavior map

Official server symbols:

- `net.minecraft.src.ItemBed.onItemUse` places block `26` only on face `1`
  (UP). Yaw `0` writes foot metadata `0` and head metadata `8` one cell south.
- `net.minecraft.src.BlockBed.blockActivated` walks to the head, then
  `EntityPlayer.goToSleep`. Night is required (`World.isDaytime` is false when
  `skylightSubtracted >= 4`). Console `time set 18000` is the lab night gate.
- `EntityPlayerMP.goToSleep` emits Packet17Sleep on `OK`. Occupied bit `4` is
  applied to the head only (`26:8` becomes `26:12`). That is the SET enter.
- `World.isAllPlayersFullyAsleep` plus `spawnHostileMobs == false` skips to the
  next morning and wakes the player with `wakeUpPlayer(false, false, true)`.
  The third flag writes player `SpawnX/Y/Z` from the occupied head. Occupied
  bit `4` clears (`26:12` back to `26:8`). The actor is standing again.
- A raised 3x3 stone pad gives `BlockBed.getNearestEmptyChunkCoordinates` a
  standable cell beside the bed. After wake, sand `12` plus cactus `81` is
  placed on the east pad. Contact keeps Packet8 health falling until `0`.
  Packet9 restores health `20` at the bed, not `level.dat` `SpawnX/Y/Z`.
- `net.minecraft.src.Packet70Bed` reason `0` is `tile.bed.notValid` on respawn
  at a missing bed. This SET keeps the bed and records Packet70 `-1`.

This map does not claim M330 occupy/wake without death, M135 wait-under-kill
respawn at world spawn, M469 void death without a bed, daytime Packet3
refusal, Nether bed explosions, or rain Packet70.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-3x3-stone+item355-block26+sand12-cactus81|cause=packet15-item355-place+empty-hand-night-use+cactus-aabb|wire=packet17-sleep+packet70=-1+packet53-occupied+packet8-health20->0+packet9-dimension-zero|oracle=bed-spawn-packet9-not-world-spawn-not-m330-occupy-only-not-m135-wait-under-kill-not-m469-void-without-bed|column=17,foot=4:72:4:26:0,head=4:72:5:26:8,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,wake=standing,death=cactus81,health=20->0->20,packet8=0,packet9=09:00,dimension=0,spawn=bed,world=not-level.dat,persisted=bed,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed`.
