# M414 behavior map

One official session places still lava `11:0` from lava bucket `327`
beside still water `9:0` from water bucket `326` in a raised two-cell
basin, then repeats that lava-source plus water collision in a second
isolated basin south of the first. Both lava-source cells harden to
obsidian `49:0`. Packet15 on the support plus direction-255 raytrace
writes each fluid. Both obsidian cells and both water cells survive a
clean save plus fresh login.

This map does not re-qualify the shipping single two-cell reaction (M139),
bucket place/pickup (M344), or obsidian-frame portal lighting (M132).
Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lava-bucket327+water-bucket326|cause=packet15-dir255-bucket327-west+packet15-dir255-bucket326-west+packet15-dir255-bucket327-south+packet15-dir255-bucket326-south|wire=packet53-lava11+packet53-water9+packet53-obsidian49+packet53-lava11+packet53-water9+packet53-obsidian49|oracle=live-west-11to49+live-south-11to49+fresh-login-two-obsidian|column=17,floor=4:71:4:1:0,adjacent=4:72:4:11:0->49:0,water-adj=3:72:4:0:0->9:0,south=4:72:6:11:0->49:0,water-south=5:72:6:0:0->9:0,held-lava=327,held-water=326,obsidian=2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`856101df96a1dea04d9f18e7a1ceef3018dce576227d046030271fa67825fbff`.
