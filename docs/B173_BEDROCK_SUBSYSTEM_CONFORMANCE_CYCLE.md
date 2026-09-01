# B173-BEDROCK-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 bedrock subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps bedrock block 7 through its native ItemBlock placement and strength-gated player-break lifecycle. It proves metadata zero and stack consumption, zero break strength with an unchanged cell and no reachable drop, chunk-NBT persistence, full collision, opaque unlit transport, unscheduled stable tick-callback behavior, and stable neighbor handling.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica places a provided bedrock item through the native Item route, applies the same strength threshold used by the server break manager, preserves the unbreakable cell and empty legal drop surface, reloads a native chunk-NBT state, measures collision and light tables, invokes the native tick callback, and applies stone and lever neighbor notifications through BedrockSubsystemFixture.

Expected signal: `family=bedrock-subsystem,subject=7,claims=9,domain=7:0,item-placement=7x1>0,break=strength-0+stable,drop=none,persistence=chunk-nbt,collision=full,light=255:0,ticks=scheduled-F+callback-stable,neighbors=stable,oracle=MATCH`.

Frozen semantic SHA-256: `92c66dfc942102809b5319da6632cb9dd22f382c12dd1db9d7472e2b5bac2e34`.
