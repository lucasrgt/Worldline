# M573-STICKY-HEAD-BREAK-SET sticky head break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M573 opens the official sticky-piston head-break leftover boundary. One headless session clones the west sticky-29 arm, Packet15-extends until sticky head 34:12 is present, then Packet14-breaks the HEAD with iron pickaxe 257. Official leftover cleanup removes sticky base 29 and drops Packet21 sticky piston 29. The head and base cells are air after a clean save plus fresh login. This is distinct from M554, which Packet14-breaks the extended regular piston 33 BASE, and from M144 sticky-piston pull by lever unpower. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the cloned sticky-29 west arm in two fresh official server JVMs. Each run extends sticky piston 29 until sticky head 34:12 is present, Packet14-breaks the HEAD with iron pickaxe 257, and reloads the leftover air cells after save plus fresh login. The frozen signal includes extend and head-break and must not include retract or regular piston 33. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,extend=29:4->12,head-break=34:12->0,base-left=29:12->0,piston=4:65:4:29:4->12->0,head=3:65:4:1:0->34:12->0:0,pushed=2:65:4:0:0->1:0->1:0,lever=5:64:4:69:1->9,drops=packet21-29,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `eb5e939b57ac0f6890a4d5bbb17ff700d690d6f8d3384748b4e3afd1ad0e0869`.
