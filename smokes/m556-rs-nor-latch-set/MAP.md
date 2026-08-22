# M556 RS-NOR latch-set behavior map

Two wall redstone torches form one official RS-NOR latch. Packet15 places
north torch `76:4` on body A and south torch `76:3` on body B. A west
repeater plus dust line from B holds A unlit `75:4` as RESET. A ground-lever
SET pulse on B's repeater input inverts B to `75:3`; after save plus login,
A is lit `76:4` and stays on after SET drops. A RESET pulse returns
`75:4` plus `76:3` and stays off. The final RESET pair survives a clean
save plus fresh login.

This map does not re-qualify M312's single north invert `76:4 -> 75:4` or
M555 torch burnout. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+rs-nor-76:4+76:3|cause=packet15-item76-north-then-south+empty-hand-packet15-set-pulse+reset-pulse|wire=packet53-q-75:4->76:4->75:4+qbar-76:3->75:3->76:3|oracle=set-stays-on+reset-stays-off+fresh-login|column=17,blockA=3:72:4:1:0,blockB=7:72:4:1:0,set=10:72:4:69:floor->on->off,reset=5:72:6:69:floor->on->off,q=3:72:3:75:4->76:4->75:4,qbar=7:72:5:76:3->75:3->76:3,stays-on=true,stays-off=true,persisted=q=75:4+qbar=76:3,clients=5,disconnect=clean
```

Frozen semantic SHA-256:
`7241b7297eea8617a084daaf981b2001119180794ec82ab3fbd7d664a55537ad`.
