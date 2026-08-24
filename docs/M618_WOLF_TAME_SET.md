# M618-WOLF-TAME-SET wolf tame set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M618 opens a bounded wild-wolf tame boundary without claiming a deterministic entity RNG draw. Animals are enabled so a default spawner 52 can be retargeted from Pig to Wolf. Packet24 type 95 receives bone item 352 through Packet7 button 0 until Packet38 status 7, with a hard limit of 64 bones. Packet40 tamed bit 2 and persisted Owner NBT must match WolfTame618. This is distinct from M420 dye 351:4, M449 anger, M468 assist, and M583 sit.

## Qualification cycle

DataDrivenCycle executes WolfTameSmoke twice on the official b1.7.3 server JAR. Each run rebuilds the raised 7x7 grass platform, places one default spawner, retargets EntityId to Wolf, permits status 6 retries within the 64-bone bound, and requires status 7 plus owner/collar metadata and clean persistence. The frozen evidence records the bound rather than the random attempt number. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type95,bone=352,bones=bounded<=64,tame=packet38-status7,collar=red,owner=WolfTame618,tamed=packet40-bit2,death=no-packet38-status3,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `548375ef25ac7b5537eed78118b10d1198ec9b2958e30fd8796f7a65c8d34af2`.
