# M563-NETHER-EXIT-CREATE-SET Nether exit create set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M563 opens the official Nether-exit create set. The actor builds an M382
obsidian `49` frame, ignites portal `90`, and enters the Nether. A relog
shifts the session east so no Overworld portal remains in the 128-block
search window. Returning through a second Nether frame makes the official
server create a new Overworld portal: fourteen obsidian `49` cells plus
six portal `90` cells.

This is distinct from M134 roundtrip reuse of the original M382 frame,
from M561 destination search on the Nether side, and from M562 pair
collapse. M563 does not claim exact created coordinates, concurrent
travelers, entity transport, death/respawn, or client rendering.
Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.

## Qualification cycle

`NetherExitCreateSetCycle` rebuilds the M382 overworld frame, traverses
to the Nether, relogs east of the generated portal, and returns through
a second Nether frame in two fresh official server JVMs. Each run requires
Packet9 `0→-1→0` and a created Overworld portal of fourteen obsidian `49`
cells plus six portal `90` cells that is not the source frame. One official
EOF is retried after a 5 second sleep. Headless `B173WireClient` is the
only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/NetherExitCreateSetCycle.java m563-nether-exit-create-set
```

Canonical evidence uses two official server JVMs and six client sessions.
The frozen semantic SHA-256 is
`6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.

Expected signal: `dimensions=0->-1->0,column=10,source=4:65:4,shift=32,created=6x90+14x49,obsidian=49,portal=90,not-source,not-m134-reuse,not-m382-activation-only,not-m561-search,not-m562-pair,cooldown=220,persisted=true,clients=3,disconnect=clean`.

Frozen semantic SHA-256: `6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.
