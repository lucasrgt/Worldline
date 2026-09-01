# B173-WOODEN-DOOR-STATE-DOMAIN-CYCLE official Beta 1.7.3 wooden-door reachable state domain

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit scenario exercises the complete wooden-door metadata domain on the unmodified official Beta 1.7.3 server. Four look-derived placements prove lower metadata 0 through 3 and upper metadata 8 through 11; four empty-hand activations prove lower metadata 4 through 7 and upper metadata 12 through 15. A fresh login proves the final open grid persisted.

## Qualification cycle

DataDrivenCycle compiles the reusable state-domain TestKit contract, the official-server provider, and one caller-owned wooden-door row. The family runs in a fresh isolated world, freezes canonical per-step evidence, and requires exact inventory consumption, all sixteen declared metadata states, a fresh-login boundary, and provider-owned shutdown.

Expected signal: `provider=b1.7.3-server-state-domain,family=wooden-door,rows=1,passed=1,states=16,reload=FRESH_LOGINx1,evidence=9d4fe0b3288e28dc61f4e3d96396442b507f01491334ac8622f12a4ef9c33c2c,isolation=1-fresh-worlds`.

Frozen semantic SHA-256: `5763b0a43e148f0c572b7e5db300482d939add4eb9be8135829e775651e5edbc`.
