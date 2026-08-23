# M582-PAINTING-SUPPORT-BREAK-SET painting support break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M582 opens the official painting support-break SET. Item 321 hangs on a raised 2x2 stone wall through Packet15, then Packet14 with iron pickaxe 257 breaks the attached support cell. Both peers observe Packet29 destroy of that painting entity and Packet21 drop of item 321. This is distinct from M177 Packet25 spawn identity, M351 opposed-face orientation, and M430 remaining motive sizes.

## Qualification cycle

DataDrivenCycle rebuilds the cloned M177 wall in two fresh official server JVMs. Each run places painting 321 on the west face, Packet14-breaks the support at the Packet25 origin, and requires Packet29 plus Packet21-321 on both protocol-14 clients. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,wall=5:72:4-5:73:5:1:0,support=5:72:4:1:0->0:0,painting=5:72:4:dir1,packet25+packet29+packet21-321,shared-id,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `10031f7cd4b860e20a01c8677180286b98d788c6ab5ce7d754879b484a81000d`.
