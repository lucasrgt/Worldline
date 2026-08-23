# M590-GHAST-FIREBALL-PUNCH-SET Ghast fireball punch set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M590 qualifies the official Beta 1.7.3 dedicated-server Nether ghast fireball punch family. A dimension -1 actor on a seeded netherrack platform with a cobble pad and Ghast spawner observes Packet24 type 56 and Packet23 type 63, then Packet7-punches that fireball while looking straight up. Protocol-14 Packet28 must then carry an upward look-aligned velocity. This SET does not re-qualify M410 spawn-only type 63 or M459 Packet60 strength-1 impact.

## Qualification cycle

DataDrivenCycle rebuilds the Nether netherrack-and-cobble platform in two fresh official server JVMs. Each run logs a dimension -1 actor, waits for Packet24 type 56 and Packet23 type 63, selects a wooden sword, looks straight up, and Packet7-attacks the fireball until Packet28 velocity is upward. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `dimension=-1,support=36:57:-14:87,pads=0,cobble-pads=1,spawner=36:58:-15:52:0,entityid=Ghast,ghast=type56,fireball=type63,thrower=ghast,punch=packet28-look-up,redirect=up,not-m410-spawn-only,not-m459-hit,packet23-known=absent,clients=3,disconnect=clean`.

Frozen semantic SHA-256: `ccf294e6ee17b1c7670374e4d95dc9de2b663e720f0b137c14a1c6436e89bdbb`.
