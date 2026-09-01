# B173-FIRE-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 fire subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps fire block 51 as a support-dependent, transparent, luminous, noncollidable random-tick block with native ItemBlock placement, infinite break strength, no drop, complete age metadata 0 through 15, direct chunk-NBT persistence, and deterministic supported-versus-support-loss neighbor behavior. Existing fire spread and native-render evidence remain independently owned and are not count-farmed here.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica places a supplied fire item on netherrack, applies the server harvest sequence, advances the native update callback through all sixteen ages with deterministic Random inputs, reloads native chunk NBT at age 15, measures collision and light tables, reads random-tick enrollment and tick rate, proves netherrack-supported stability, and removes stone support to trigger causal extinguishing through FireSubsystemFixture.

Expected signal: `family=fire-subsystem,subject=51,claims=8,domain=51:0..15,item-placement=51x1>0,break=infinite+removed,drop=none,persistence=chunk-nbt,collision=none,light=0:15,ticks=random-T+age-0..15+rate-40,neighbors=supported+support-loss,oracle=MATCH`.

Frozen semantic SHA-256: `35252ba59f92c8784bad3e2b729367cb5fc0f49e30e4c00192f77130ba03a971`.
