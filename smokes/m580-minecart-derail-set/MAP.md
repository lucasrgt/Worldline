# M580 behavior map

One official session builds a raised north-south dead-end: a stone wall north of
powered-rail item `27`, regular rail `66` one cell south, wooden plate `72` one
cell south of that rail, and a stone bumper south of the plate. Packet15 of
minecart item `328` on the still unpowered rail emits Packet23 type `10` at the
powered-rail center, not the plate. A bounded live hold keeps plate `72:0`
idle. Packet15 of floor torch item `76` then writes `27:8`; the cart travels
onto rail `66` and derails off that dead end onto plate `72:1`. Those exact
cells survive a clean save plus fresh login.

This map does not re-qualify M155 spawn-only Packet23 type `10` or M377
powered-rail launch onto detector `28`. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wall+powered-rail27+rail66+plate72+bumper+torch76+minecart328|cause=packet15-item27+packet15-item66+packet15-item72+packet15-minecart328+packet15-item76|wire=packet23-type10+thrower0+packet53-rail27:0->8+packet53-plate72:0->1+packet53-torch76:5|oracle=unpowered-hold-idle+derail-off-rail66-onto-plate+fresh-login|column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,rail=4:72:4:27:0->8,track=4:72:5:66:0,plate=4:72:6:72:0->1,cart=type10+thrower0+fixed144:2331:144,unpowered-hold=idle,derail=1,torch=5:72:4:76:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9a06308be99ac4a6a13c76abece74b644f9ea0a60d00a90359bfe184f77bce87`.
