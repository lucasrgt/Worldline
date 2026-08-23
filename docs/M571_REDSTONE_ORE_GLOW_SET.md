# M571-REDSTONE-ORE-GLOW-SET redstone ore glow set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Stepping on or clicking placed redstone ore lights unlit 73:0 to glowing 74:0, then official random ticks darken it back to 73:0. This is distinct from M229 unlit placement persistence and M511-SW controlled trigger membership.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone east-floor ore in two fresh official server JVMs. Each run places redstone ore 73, empty-hand Packet15 lights it to 74, waits official random ticks, then a fresh login proves darkened 73:0. Walking onto that cell lights it again; a second save plus login proves the later darken. Headless protocol-14 B173WireClient only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,perch=3:71:4:1:0,ore=5:71:4:73:0,click=73:0->74:0,click-dark=74:0->73:0,step=73:0->74:0,step-dark=74:0->73:0,persisted=true,clients=3,disconnect=clean`.

Frozen semantic SHA-256: `0f6e8216f809dfc39632b18769fef8f62e2c12e4481958e5f135b515484f8098`.
