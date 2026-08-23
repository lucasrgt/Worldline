# M583-WOLF-SIT-SET wolf sit set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

A tamed wolf sits on owner right-click and stands on a second click. Packet40 index 16 bit 0 is the sitting flag. This is distinct from M449 wild-wolf anger and M468 tamed assist.

## Qualification cycle

DataDrivenCycle rebuilds the raised grass platform in two fresh official server JVMs. Each run places one default spawner 52, retargets EntityId to Wolf, tames Packet24 type 95 with bone 352 until Packet38 status 7, unsits with stick 280, then sits and stands on successive Packet7 button 0 clicks. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type95,bone=352,tame=packet38-status7,sit=packet7-button0+packet40-sit,stand=packet7-button0+packet40-stand,held=280,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `56eb02100c063bedf32f982e6d7e66bd756ad07848f21c62443cd90b26786659`.
