# B173-LOCKED-CHEST-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 locked-chest subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps locked chest block 95 as a luminous full cube with native ItemBlock placement, instant harvest and one self-drop, direct chunk-NBT persistence, random-tick enrollment, self-removing native tick behavior, and stable ordinary neighbor notifications. The package also corrects the Functional Census taxonomy: BlockLockedChest is not a container and owns no tile entity.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica places a supplied locked-chest item through the native Item route, applies the same strength and harvest operations as the server break manager, identifies the exact dropped stack, reloads native chunk NBT, measures collision and light tables, proves random-tick enrollment and the direct self-removal callback, and applies stone and lever neighbor notifications through LockedChestSubsystemFixture.

Expected signal: `family=locked-chest-subsystem,subject=95,claims=9,domain=95:0,item-placement=95x1>0,break=infinite+removed,drop=95x1,persistence=chunk-nbt,collision=full,light=255:15,ticks=random-T+callback-remove,neighbors=stable,oracle=MATCH`.

Frozen semantic SHA-256: `eeab74a9f384d7557e7f38fa181fcfc8a7f579205a245f495f8ba16d89f81374`.
