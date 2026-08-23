<!-- worldline-map-schema=1 -->
<!-- boundary=m158-bed -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ab95c0893977d3774ddf9672b77063db206c52479e9645e917f6f0d42d49f2f0 -->

# M158 behavior map

Official server symbols:

- `net.minecraft.src.ItemBed.onItemUse` places block `26` only on face `1`
  (UP). Yaw `0` writes foot metadata `0` and head metadata `8` one cell south.
- `net.minecraft.src.BlockBed.blockActivated` walks to the head, then
  `EntityPlayer.goToSleep`. Daytime (`World.isDaytime`, `skylightSubtracted < 4`)
  returns `EnumStatus.NOT_POSSIBLE_NOW` and Packet3 `tile.bed.noSleep`.
- `EntityPlayerMP.goToSleep` emits Packet17Sleep on `OK`. Occupied bit `4` is
  applied to the head only.
- `World.isAllPlayersFullyAsleep` plus `spawnHostileMobs == false` skips to the
  next morning and wakes the player.
- `net.minecraft.src.Packet70Bed` reason `0` is `tile.bed.notValid` on respawn
  at a missing bed. Reasons `1`/`2` are rain start/stop. Sleep refusal is not
  Packet70 on this dedicated server.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item355-block26|cause=packet15-empty-hand-bed|wire=packet3-noSleep+packet17-sleep+packet53-occupied|oracle=night-occupy+smp-skip|column=17,foot=4:72:4:26:0,head=4:72:5:26:8,day=noSleep,occupy=4:72:5:26:12,packet17=head,packet70=-1,skip=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ab95c0893977d3774ddf9672b77063db206c52479e9645e917f6f0d42d49f2f0`.
