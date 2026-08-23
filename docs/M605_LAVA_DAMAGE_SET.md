# M605-LAVA-DAMAGE-SET lava damage set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Standing in still lava 11:0 deals repeated Packet8 health drops while the player stays alive. The first accepted hit is 20 to 16; the next is 16 to 13. Vanilla typically also ignites the player. This is distinct from M138 lava place/flow and from M465 lava death to health 0.

## Qualification cycle

DataDrivenCycle rebuilds a raised stone basin of still lava 11:0 in two fresh official server JVMs. Each run places lava bucket 327, stands in the source, records two Packet8 drops 20 to 16 to 13 with Packet38 status 2, then leaves so health 13 persists across a fresh login. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,floor=4:71:4:1:0,lava=4:72:4:11:0,health=20->16->13,damage=4+3,hits=2,status=2,alive=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `ee5b9cd5369a69732b8def74eb28c5cb2ea094f821a491fc7e3c7c5937945a99`.
