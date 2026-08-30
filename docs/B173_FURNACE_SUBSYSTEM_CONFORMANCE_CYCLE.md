# B173-FURNACE-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 furnace subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps idle furnace 61 and lit furnace 62 as a stateful tile machine. It proves four orientations, ignition, a complete sand-to-glass smelt, extinction, active break and inventory drops, chunk-NBT progress recovery, full collision, light, and stable neighbor handling.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Each replica materializes both domains, records nonzero burn and cook progress, completes exactly 200 tile ticks, reloads active block and inventory state through native chunk NBT, breaks a populated active furnace, and compares the complete canonical trace through FurnaceSubsystemFixture.

Expected signal: `family=furnace-subsystem,subjects=2,claims=11,domains=61+62:2..5,materialization=item61+smelt,drop=61+contents,persistence=chunk-nbt-progress,collision=full+full,light=0+13,ticks=tile-200,neighbors=stable+orientation,oracle=MATCH`.

Frozen semantic SHA-256: `81a14731a028188bc83b9a9b7c4637bb939c932e77fae97c31573830a8be100d`.
