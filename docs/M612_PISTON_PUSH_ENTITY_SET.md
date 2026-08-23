# M612-PISTON-PUSH-ENTITY-SET piston push entity set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M612 opens the official piston-push-entity boundary on protocol-14. A west-facing piston 33 extends into air and displaces a cobble item entity, so fresh-login Packet21 coordinates move west. This is distinct from M142/M367 block motion and M546 quasi-connectivity. It does not claim sticky pull, BUD, player-pose rubber-banding, or a generic piston model. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the west piston and cobble drop in two fresh official server JVMs. Each run Q-drops cobble 4 into the air head cell, Packet15-extends piston 33, and reloads the displaced Packet21 item after save plus fresh login. The frozen signal includes dx-west=true. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,extend=33:4->12,head=0:0->34:4,item=4x1:0,dx-west=true,piston=4:65:4:33:4->12,head-cell=3:65:4:0:0->34:4,pushed=2:65:4:0:0,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `f34c4db7ef2aa8add3324a61994478d128f556aa31a3f55a6e70bdc348377f3f`.
