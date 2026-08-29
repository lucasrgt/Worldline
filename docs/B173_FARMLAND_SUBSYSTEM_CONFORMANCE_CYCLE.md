# B173-FARMLAND-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 farmland subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps farmland block 60 as a stateful random-tick soil block with native ItemBlock placement, finite break strength, one dirt drop, complete moisture metadata 0 through 7, hydrated chunk-NBT persistence, full collision despite fifteen-sixteenths visual height, opacity 255 with zero emission, and deterministic air-above stability versus solid-cover conversion to dirt. Existing tick-policy and native-render evidence remain independently owned and are not count-farmed here.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica places a supplied farmland item, applies the server harvest sequence and identifies the exact dirt drop, hydrates metadata 0 to 7 with adjacent water, serializes native chunk NBT, removes the water and deterministically traverses moisture 7 to 0, measures collision bounds, visual height, cube flags and light tables, proves random-tick enrollment, and contrasts ordinary neighbor stability with causal solid-cover conversion through FarmlandSubsystemFixture.

Expected signal: `family=farmland-subsystem,subject=60,claims=8,domain=60:0..7,item-placement=60x1>0,break=finite+removed,drop=3x1,persistence=chunk-nbt,collision=full-vs-15/16,light=255:0,ticks=random-T+hydrate+dry,neighbors=air-stable+solid-cover-dirt,oracle=MATCH`.

Frozen semantic SHA-256: `7e3ab53109572ad7404382a8ecc0492abb956eb664e0f807e6e5abb731b860ee`.
