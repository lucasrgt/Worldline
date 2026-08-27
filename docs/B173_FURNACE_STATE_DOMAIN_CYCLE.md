# B173-FURNACE-STATE-DOMAIN-CYCLE official Beta 1.7.3 furnace facing state domain

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit scenario exercises the complete unlit-furnace facing metadata domain on the unmodified official Beta 1.7.3 server. Four look-derived placements prove metadata 2 through 5, and a fresh login proves the complete facing grid persisted.

## Qualification cycle

DataDrivenCycle compiles the reusable state-domain TestKit contract, the official-server provider, and one caller-owned furnace row. The family runs in a fresh isolated world, freezes canonical per-step evidence, and requires exact inventory consumption, all four declared facing states, a fresh-login boundary, and provider-owned shutdown.

Expected signal: `provider=b1.7.3-server-state-domain,family=furnace,rows=1,passed=1,states=4,reload=FRESH_LOGINx1,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=1-fresh-worlds`.

Frozen semantic SHA-256: `0000000000000000000000000000000000000000000000000000000000000000`.
