# M330 behavior map

Official server symbols:

- `net.minecraft.src.ItemBed.onItemUse` places block `26` only on face `1`
  (UP). Yaw `0` writes foot metadata `0` and head metadata `8` one cell south.
- `net.minecraft.src.BlockBed.blockActivated` walks to the head, then
  `EntityPlayer.goToSleep`. Night is required (`World.isDaytime` is false when
  `skylightSubtracted >= 4`). Console `time set 18000` is the lab night gate.
- `EntityPlayerMP.goToSleep` emits Packet17Sleep on `OK`. Occupied bit `4` is
  applied to the head only (`26:8` becomes `26:12`). That is the SET enter.
- `World.isAllPlayersFullyAsleep` plus `spawnHostileMobs == false` skips to the
  next morning and wakes the player, clearing the occupied bit (`26:12` back
  to `26:8`). The actor is standing again. That is the SET leave.
- `net.minecraft.src.Packet70Bed` is tracked beside Packet17. Reason `0` is
  `tile.bed.notValid` on respawn at a missing bed. Reasons `1`/`2` are rain
  start/stop. Sleep enter on this dedicated server is Packet17, not Packet70.
  The Packet70Bed tracker records `-1` for this Overworld occupy.

This map does not claim daytime Packet3 refusal, Nether bed explosions, rain
Packet70, occupied chat, spawn-point selection after death, or client bed
rendering. It is distinct from M158 daytime refusal and from M240 place-only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item355-block26|cause=packet15-item355-place+empty-hand-night-use|wire=packet17-sleep+packet70=-1+packet53-occupied|oracle=sleep-enter+leave-standing+persisted-wake|column=17,foot=4:72:4:26:0,head=4:72:5:26:8,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,wake=standing,skip=true,persisted=wake,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1415f89a64178b9c0135d108239ba04eb9fca293f9d8ee9005347624eb6842af`.
