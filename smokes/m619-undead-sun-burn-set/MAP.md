# M619-UNDEAD-SUN-BURN-SET undead sun burn set behavior map

Two official arms share one raised `7×7` grass pad and one mob spawner
`52:0`. The open arm leaves the spawner under sky. The cover arm adds a
`5×5` stone roof over the pad. After each clean save, region NBT
`EntityId` becomes `Zombie` and `Delay` becomes `1`.

Console time `14000` yields Packet24 type `54` with Packet40 fire flags
`0`. Console time `6000` then sets Packet40 flags bit 0 in the open arm
and leaves flags `0` under the roof.

This map does not claim natural night spawns (M435), spawner delay/range
(M569), or player Packet8 fire damage (M276). Headless `B173WireClient`
protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52+5x5-center-roof|cause=nbt-entityid-zombie+nbt-delay-1+time-14000-then-6000|wire=packet40-flags0-night+packet40-flags1-day-open+packet40-flags0-day-cover|oracle=undead-sun-burn-not-m435-natural|column=17,platform=7x7-48grass,open=4:72:4:52:0,cover=4:72:4:52:0,entityid=Zombie,night=14000,day=6000,night-fire=0,day-open=type54-flags1,day-cover=type54-flags0,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`54f2215e595c43a358e5cf702f0fdd19353f28c34bdf1f5710d664454f18672f`.
