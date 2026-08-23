# M604-DROWNING-SET drowning set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M604 opens the official drowning Packet8-damage SET. Two-deep still water over the eye empties the air meter and deals Packet8 20->18 with Packet38 status 2. Health stays 18. This is distinct from M465 lava+drown+suffocate death to 0 and from M307 compound drown+suffocate+lava hurt.

## Qualification cycle

DataDrivenCycle rebuilds the two-deep still-water fixture in two fresh official server JVMs. Each run submerges Drown604, waits through the air-meter window with WorldlineSmokeAwait, then requires Packet8 20->18 and Packet38 status 2 without death. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and two client sessions. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `cause=drown,column=17,water=4:72:4+4:73:4,health=20->18,damage=2,status=2,packet8=18,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `be6df540130ff955f1e6aa69fc938d51bcf8c9f3e219730af9ff0207ee88bc5d`.
