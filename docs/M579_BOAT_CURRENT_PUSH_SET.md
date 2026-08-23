# M579-BOAT-CURRENT-PUSH-SET boat current push

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

A boat on flowing water is displaced downstream. Two headless protocol-14 peers decode the same Packet23 boat object, then Packet31, Packet33, or Packet34 motion whose quantized pose moves with the current. This is distinct from M154 boat spawn, M378 still-water ride attach/detach, and M403 boat-break drops.

## Qualification cycle

DataDrivenCycle rebuilds a raised east-flowing water channel in two fresh official server JVMs. Each run places still-water 9 at the west end, opens a dirt gate so flowing water 8 occupies the interior cell, places boat 333 there, and correlates peer entity-move packets downstream. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,source=4:72:4:9:0,flow=6:72:4:8:2,boat=type1+shared-id+packet23+packet31|33|34-downstream,spawn=208:2313:144,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `8f1abed600a6a2597af5298ac5a9d410beadf7afc3fdd328745db2eb2236359e`.
