# M595-POWERED-RAIL-BRAKE-SET powered rail brake set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

An unpowered powered-rail stops a moving minecart. This is distinct from M184 unpowered powered-rail placement and from M377 powered-rail acceleration onto a detector.

## Qualification cycle

DataDrivenCycle runs PoweredRailBrakeSetSmoke twice on the official b1.7.3 server. Packet15 places a north-walled launch rail 27, a mid rail 27, and a beyond detector 28. Minecart 328 launches when floor torch 76 powers both rails. Breaking the torch unpowers those rails; the moving cart stops and the beyond detector stays 28:0. Fresh login keeps the unpowered rails and idle detector. Headless protocol-14 B173WireClient only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,wall=4:72:3:1:0,bumper=4:72:7:1:0,launch=4:72:4:27:0->8->0,mid=4:72:5:27:0->8->0,beyond=4:72:6:28:0,cart=type10+thrower0+fixed144:2331:144,moved=1,braked=1,torch=5:72:4:76:5->0,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `9af61c7c0e4b8e165e1a7d94f70410c5bff7e66102a757de60a02f08077ffa38`.
