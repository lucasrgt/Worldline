# M519-SW-DISPENSER-RNG-MEMBERSHIP Sw dispenser rng membership

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

M519 compares seeded dispenser reservoir sampling across mapped and official
Beta 1.7.3 tile entities. Fourteen draws must select only occupied inventory
members and remove exactly one item; single-slot and empty controls close the
boundary.

The expected signature is
`ee4be352a5e761e0091c6c9206d8771151003ad7b2ba731c4b32bf6455dfc8fe`.
Qualify it with `java tools/harness/Gate.java --milestone
m519-sw-dispenser-rng-membership`.

Expected signal: `oracle=MATCH,fixture=m519-sw-dispenser-rng-membership,ticks=14,controlled=true`.

Frozen semantic SHA-256: `ee4be352a5e761e0091c6c9206d8771151003ad7b2ba731c4b32bf6455dfc8fe`.
