# M585-ARMOR-DURABILITY-HIT-SET armor durability hit

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Worn armor loses durability after a hostile melee hit. An NBT-seated iron chestplate 307 in window-0 slot 6 starts at damage 0. One Packet24 type-54 zombie melee at Normal difficulty 2 produces Packet8 and Packet103 that raise that damage. This is distinct from armor-reduction (M451), equip-only window proofs (M270-M273), crafts, and PvP Packet7 (M66).

## Qualification cycle

DataDrivenCycle rebuilds the raised grass platform in two fresh official server JVMs. Each run places one default spawner 52, retargets EntityId to Zombie, seats iron chestplate 307 through player NBT slot 102, and records Packet103 slot-6 damage after one type-54 melee. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 is the only client. There is no GUI and no Aero path.

Expected signal: `column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Zombie,mob=type54,night=14000,armor=307,slot=6,before=0,after=2,hit=20->19:1,food=322+320,wire=packet24-type54+packet8+packet103,not-m451-reduction,not-craft,not-equip-only,not-m66-pvp,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `a8ddc5a76726f9c1afd03c7f4dcbf222f0b8197112369ff2f8e5630a3c31b6c3`.
