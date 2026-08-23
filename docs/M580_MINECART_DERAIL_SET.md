# M580-MINECART-DERAIL-SET minecart derail set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M580 opens the official minecart-derail boundary. A moving minecart item 328 that leaves the end of regular rail 66 derails onto wooden plate 72. Unpowered powered-rail 27 holds the cart idle so the plate stays 72:0; torch 76 then writes 27:8 and the cart leaves 66 onto 72:1. This is distinct from M155 spawn-only Packet23 type 10 and from M377 powered-rail launch onto detector 28. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the dead-end rail in two fresh official server JVMs. Each run places powered rail 27, regular rail 66, wooden plate 72, and a south bumper, spawns Packet23 type 10 on unpowered 27, proves plate 72:0 while idle, then powers 27:8 so the cart derails onto 72:1. WorldlineSmokeAwait observes the idle window and awaits the plate. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,rail=4:72:4:27:0->8,track=4:72:5:66:0,plate=4:72:6:72:0->1,cart=type10+thrower0+fixed144:2331:144,unpowered-hold=idle,derail=1,torch=5:72:4:76:5,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `9a06308be99ac4a6a13c76abece74b644f9ea0a60d00a90359bfe184f77bce87`.
