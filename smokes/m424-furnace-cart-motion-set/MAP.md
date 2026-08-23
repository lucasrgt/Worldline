<!-- worldline-map-schema=1 -->
<!-- boundary=m424-furnace-cart-motion-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=536398b8e8c64ca3dc8e527842ae556bf4175363fc0b8e554d2ba0ec52811b1b -->

# M424 behavior map

One official session builds a raised north-south track: a stone wall north of
rail item `66`, a second `66` cell one cell south, and detector rail item `28`
one cell south of that, with a stone bumper beyond the detector. Packet15 of
furnace-minecart item `343` on the first rail emits Packet23 type `12` at that
rail center, not the detector. A bounded live hold keeps detector `28:0` idle
while the cart is unfueled. Packet7 of coal item `263` from north of the cart
consumes the stack and pushes the furnace cart south on rail `66`. Occupancy
bit 8 then writes `28:8`. Those exact cells survive a clean save plus fresh
login.

This map does not re-qualify M257 spawn-only type `12` or M377 powered-rail
type `10` motion. It reuses `awaitObjectSpawn(12)` and does not add a second
Packet23 tracker. Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wall+rail66+detector28+bumper+furnace-minecart343+coal263|cause=packet15-item66+packet15-furnace-minecart343+packet7-coal263|wire=packet23-type12+thrower0+packet103-coal263-consume+packet53-detector28:0->8|oracle=unfueled-hold-idle+fueled-launch-on-rail66+fresh-login|column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,rail=4:72:4:66:0,track=4:72:5:66:0,detector=4:72:6:28:0->8,cart=type12+thrower0+fixed144:2331:144,coal=263:1->0,unfueled-hold=idle,fueled=1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`536398b8e8c64ca3dc8e527842ae556bf4175363fc0b8e554d2ba0ec52811b1b`.
