# M377 behavior map

One official session builds a raised north-south track: a stone wall north of
powered-rail item `27`, detector rail item `28` one cell south, and a stone
bumper south of that detector. Packet15 of minecart item `328` on the still
unpowered rail emits Packet23 type `10` at the powered-rail center, not the
detector. A bounded live hold keeps detector `28:0` idle. Packet15 of floor
torch item `76` then writes `27:8`; the cart launches onto the detector and
occupancy bit 8 writes `28:8`. Those exact cells survive a clean save plus
fresh login.

This map does not re-qualify M309 rail-power place (`27:8` beside torch with
the cart spawned on the detector) or M310 vehicle-rides spawn/attach. It
reuses `awaitObjectSpawn(10)` and does not add a second Packet23 tracker.
Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wall+powered-rail27+detector28+bumper+torch76+minecart328|cause=packet15-item27+packet15-item28+packet15-minecart328+packet15-item76|wire=packet23-type10+thrower0+packet53-rail27:0->8+packet53-detector28:0->8+packet53-torch76:5|oracle=unpowered-hold-idle+powered-launch-onto-detector+fresh-login|column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:6:1:0,rail=4:72:4:27:0->8,detector=4:72:5:28:0->8,cart=type10+thrower0+fixed144:2331:144,unpowered-hold=idle,powered=1,torch=5:72:4:76:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c383cb26d4289064f7ced386bb9c7cfc9cdb68545275f438464e17ef5a161977`.
