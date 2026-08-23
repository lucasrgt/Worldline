# M603-SUFFOCATION-SET suffocation set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M603 opens the official head-in-solid suffocation SET. Falling sand 12 on the standing actor's head cell deals Packet8 20->19 with Packet38 status 2. Health stays 19. This is distinct from M465 lava+drown+suffocate death to 0 and from M307's compound drown+suffocate+lava hurt. Dirt and other normal cubes share the same in-wall path; this SET oracles sand bury.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone falling-sand head fixture in two fresh official server JVMs. Each run places sand 12 so it falls into the body then head cells, then requires Packet8 20->19 and Packet38 status 2 without death. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and two client sessions. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `cause=suffocate,column=17,head=4:73:6:12:0,body=4:72:6:12:0,health=20->19,packet8=19,status=2,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `44cc512b3c80b3718cd2020ff5c519953203cef75b1a8b455fc22ed6213beae7`.
