# B173-WOODEN-DOOR-STATE-DOMAIN-CYCLE official Beta 1.7.3 wooden-door reachable state domain

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit scenario exercises the complete wooden-door metadata domain on the unmodified official Beta 1.7.3 server. Four look-derived placements prove lower metadata 0 through 3 and upper metadata 8 through 11; four empty-hand activations prove lower open metadata 4 through 7 while preserving the upper cells. A fresh login proves the final open grid persisted.

## Qualification cycle

DataDrivenCycle compiles the reusable state-domain TestKit contract, the official-server provider, and one caller-owned wooden-door row. The family runs in a fresh isolated world, freezes canonical per-step evidence, and requires exact inventory consumption, all twelve declared metadata states, a fresh-login boundary, and provider-owned shutdown.

Expected signal: `provider=b1.7.3-server-state-domain,family=wooden-door,rows=1,passed=1,states=12,reload=FRESH_LOGINx1,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=1-fresh-worlds`.

Frozen semantic SHA-256: `0000000000000000000000000000000000000000000000000000000000000000`.
