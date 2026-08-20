# M344 behavior map

One official session places still water `9:0` from water bucket `326` into
a raised four-wall stone basin and picks that source back up, then places
still lava `11:0` from lava bucket `327` into the same empty basin and
picks that source back up. Packet15 on the support plus direction-255
raytrace writes each fluid; Packet15 on the fluid cell plus direction-255
recovers the filled bucket. The empty basin, water bucket `326:1:0`, and
lava bucket `327:1:0` survive a clean save plus fresh login.

This map does not re-qualify the shipping 1:1 pickup-only (M168/M181) or
place-only (M254/M255) traces. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+water-bucket326+lava-bucket327|cause=packet15-support+packet15-dir255-bucket326+packet15-water-cell+packet15-dir255-bucket325+packet15-basin-cell+packet15-dir255-bucket327+packet15-lava-cell+packet15-dir255-bucket325|wire=packet53-water9+packet103-bucket325+packet53-air0+packet103-bucket326+packet53-lava11+packet103-bucket325+packet53-air0+packet103-bucket327|oracle=live-place-pickup-326/9+327/11+fresh-login-empty-basin|column=17,floor=4:71:4:1:0,water=4:72:4:0:0->9:0->0:0,held-water=326:1:0->325:1:0->326:1:0,lava=4:72:4:0:0->11:0->0:0,held-lava=327:1:0->325:1:0->327:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fe76fdf6b8ec887d8efc4caa81ce926b3efad2a42207cbefd9b6a21f9b66b789`.
