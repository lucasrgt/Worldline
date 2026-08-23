# M619-UNDEAD-SUN-BURN-SET undead sun burn set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M619 opens the official undead sunlight combustion SET. A fixture zombie (type 54) in open sky catches Packet40 fire flag 1 after server.setTime 6000, while the same undead stay unburned at night 14000 and under a 5x5 stone roof at day. This is distinct from M435 natural spawns; the spawner is fixture only.

## Qualification cycle

DataDrivenCycle executes UndeadSunBurnSetSmoke twice on fresh official server JVMs. Each run builds an open zombie-spawner pad and a roofed pad, rewrites EntityId and Delay, proves night Packet40 flags 0, then day open fire versus shaded absence. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=7x7-48grass,open=4:72:4:52:0,cover=4:72:4:52:0,entityid=Zombie,night=14000,day=6000,night-fire=0,day-open=type54-flags1,day-cover=type54-flags0,clients=4,disconnect=clean`.

Frozen semantic SHA-256: `54f2215e595c43a358e5cf702f0fdd19353f28c34bdf1f5710d664454f18672f`.
