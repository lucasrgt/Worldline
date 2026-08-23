# M616-PORTAL-SEARCH-RADIUS-SET portal search radius set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M616 opens the official nether portal search-radius boundary. An Overworld portal whose 8:1 destination sits 32 Nether blocks from an existing frame links to that frame inside the 128-block search window instead of creating a new 49+90 frame. The existing interior is outside the 16-block create window, so arrival there with zero new portal 90 at the scaled destination is search rather than create. This is distinct from M560 8:1 coordinate scale, from M563 missing-exit creation, and from M562 portal-pair collapse. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the search fixture in two fresh official server JVMs. Each run ignites the M382 Overworld frame, travels 0 to -1 to create the Nether exit, then builds a second Overworld frame whose scaled destination is 32 Nether blocks east. Packet9 0 to -1 lands on the existing interior with created=0. WorldlineSmokeAwait observes travel and destination chunks. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and six client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `dimensions=0->-1,shift=32,search=existing,radius=128,create-window=16,found=6x90,created=0,obsidian=49,portal=90,not-m560-scale-only,not-m563-create,not-m562-pair,persisted=true,clients=3,disconnect=clean,packet9=0->-1`.

Frozen semantic SHA-256: `b02e249055f3b7e33408a01b9ff5d87260c5eaf3e048ec11833a224d36a507f1`.
