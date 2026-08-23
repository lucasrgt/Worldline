<!-- worldline-map-schema=1 -->
<!-- boundary=m460-monster-bed-interrupt-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=fa58ada55be2832285f313973cf389f37a678482be07caf247515cebc8e150af -->

# M460 behavior map

Official server symbols:

- `net.minecraft.src.ItemBed.onItemUse` places block `26` only on face `1`
  (UP). Yaw `0` writes foot metadata `0` and head metadata `8` one cell south.
- `net.minecraft.src.BlockBed.blockActivated` walks to the head, then
  `EntityPlayer.goToSleep`. Night is required. Console `time set 14000` is
  the lab night gate used with hostiles.
- `EntityPlayerMP.goToSleep` emits Packet17Sleep on `OK`. Occupied bit `4`
  is applied to the head only (`26:8` becomes `26:12`). That is the SET enter.
- `TileEntityMobSpawner` plus saved `EntityId` `Zombie` emits Packet24 type
  `54` near the bed. A 24-block fence perimeter and arena-contained spawn
  selection keep that attacker on the platform. `EntityPlayer.attackEntityFrom` while sleeping calls
  `wakeUpPlayer`, clearing the occupied bit (`26:12` back to `26:8`) without
  skipping to morning. That is the SET leave.
- `net.minecraft.src.Packet70Bed` is tracked beside Packet17. Reason `0` is
  `tile.bed.notValid` on respawn at a missing bed. Reasons `1`/`2` are rain
  start/stop. This Overworld interrupt is Packet17 enter plus occupied-bit
  leave, not rain Packet70. The tracker records `-1` at occupy.

This map does not claim M330 occupy/wake with `spawn-monsters=false` morning
skip, M359 Nether bed explosions, M431 remaining bed facings, or rain
Packet70.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|profile=spawn-monsters-true|fixture=raised-7x7-grass-platform+fence85-arena+item355-block26+spawner52|cause=nbt-entityid-zombie+time-14000+packet15-empty-hand-night-use+hostile-attack|wire=packet17-sleep+packet70=-1+packet24-type54+packet53-occupied-clear|oracle=sleep-enter+monster-interrupt-leave-not-morning-skip|column=17,arena=fence85-24,foot=4:72:4:26:0,head=4:72:5:26:8,spawner=6:72:4:52:0,entityid=Zombie,mob=type54,night=14000,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,interrupt=type54,skip=false,wake=standing,persisted=leave,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fa58ada55be2832285f313973cf389f37a678482be07caf247515cebc8e150af`.
