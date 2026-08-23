# M596-FURNACE-CART-PUSH-SET furnace cart push set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

A fueled furnace minecart consumes coal 263 and self-propels south on regular rail 66. Packet15 of furnace-minecart item 343 is observed as Packet23 type 10, 11, or 12 from the official oracle, not assumed. An unfueled hold stays idle; Packet7 of coal then consumes the stack and Packet28 carries a southward velocity. This is distinct from M257 spawn-only type 12 and from M424 detector occupancy 28:0->8. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone track in two fresh official server JVMs. Each run places rail 66, furnace-minecart 343, and coal 263. The oracle object type is recorded from Packet23, coal 263 is consumed, and Packet28 proves south self-propulsion on rail 66 without detector 28 or powered rail 27. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,rail=4:72:4:66:0,track=4:72:5:66:0,end=4:72:6:66:0,cart=type12+thrower0+fixed144:2331:144,coal=263:1->0,unfueled-hold=idle,push=south,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `85b06a12ed93aa87614723a204c8f89a318c0fdc443933ffc5860da62949a8f6`.
