# B173-FURNACE-STATE-DOMAIN-CYCLE official Beta 1.7.3 furnace facing state domain

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit scenario exercises the complete unlit-furnace facing metadata domain on the unmodified official Beta 1.7.3 server. Four look-derived placements prove metadata 2 through 5, and a fresh login proves the complete facing grid persisted.

## Qualification cycle

DataDrivenCycle compiles the reusable state-domain TestKit contract, the official-server provider, and one caller-owned furnace row. The family runs in a fresh isolated world, freezes canonical per-step evidence, and requires exact inventory consumption, all four declared facing states, a fresh-login boundary, and provider-owned shutdown.

Expected signal: `provider=b1.7.3-server-state-domain,family=furnace,rows=1,passed=1,states=4,reload=FRESH_LOGINx1,evidence=40df8be86993ed586df493f558c5aa092afc59a7c4e23d23617cb36d6b59d3bd,isolation=1-fresh-worlds`.

Frozen semantic SHA-256: `ef4ea5c12cbe52244e0ecc9649fdfc57638a38f5eb9bed5d4dbb69261476bf6c`.
