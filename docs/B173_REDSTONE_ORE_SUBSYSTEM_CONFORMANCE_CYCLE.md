# B173-REDSTONE-ORE-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 redstone ore subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps inactive redstone ore 73 and glowing redstone ore 74 as one state machine. It proves causal click activation, the glowing lifecycle and bounded dust drop, chunk-NBT persistence of both states, full collision, opacity and emission, random-tick fade, and stable neighbor handling.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica resolves both registry entries, activates 73:0 into 74:0 through the native click path, fades 74:0 through its random-tick callback, breaks a glowing cell, reloads both states through native chunk NBT, measures collision and light tables, and applies stone and lever neighbor notifications through RedstoneOreSubsystemFixture.

Expected signal: `family=redstone-ore-subsystem,subjects=2,claims=13,registry=73+74,domains=73:0+74:0,materialization=click-73>74,drop=331x4..5,persistence=chunk-nbt-both,collision=full+full,light=0+9,ticks=FT+fade,neighbors=stable,oracle=MATCH`.

Frozen semantic SHA-256: `f3520bbd8dfa2735748b7a21b8ed9c2d6015710042202f4e0091eb154621deb9`.
