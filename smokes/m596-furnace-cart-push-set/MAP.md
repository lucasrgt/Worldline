# M596 furnace cart push behavior map

One official session builds a raised north-south track: a stone wall north of
rail item `66`, two more `66` cells south, and a stone bumper beyond the last
rail. Packet15 of furnace-minecart item `343` on the first rail emits Packet23
whose object type is observed from the official oracle among minecart types
`10`, `11`, and `12`, with thrower `0` and a fixed-point pose at that rail
center. A telemetered live hold keeps the unfueled cart idle. Packet7 of coal
item `263` from north of the cart consumes the stack and Packet28 carries a
southward velocity on rail `66`. Those exact rail and stone cells survive a
clean save plus fresh login.

This map does not re-qualify M257 spawn-only type `12` or M424 detector
occupancy `28:0->8`. It does not claim powered rail `27`, riding, chest carts,
derail, or collision. Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wall+rail66+bumper+furnace-minecart343+coal263|cause=packet15-item66+packet15-furnace-minecart343+packet7-coal263|wire=packet23-observed-minecart+thrower0+packet103-coal263-consume+packet28-south|oracle=unfueled-hold-idle+fueled-south-push-on-rail66-not-detector|column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,rail=4:72:4:66:0,track=4:72:5:66:0,end=4:72:6:66:0,cart=type12+thrower0+fixed144:2331:144,coal=263:1->0,unfueled-hold=idle,push=south,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`85b06a12ed93aa87614723a204c8f89a318c0fdc443933ffc5860da62949a8f6`.
