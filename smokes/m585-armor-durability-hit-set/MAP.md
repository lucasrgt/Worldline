# M585 behavior map

A raised `7x7` grass platform with a one-block stone rim seats iron
chestplate `307` through player NBT slot `102`, which occupies personal
window-0 slot `6` at damage `0`. One default spawner `52` is retargeted
to `Zombie`. At explicit Normal difficulty `2`, the Packet24 type `54`
melee is absorbed without a damaging Packet7 poke. Packet8 records the
reduced health drop `20->19` (damage 1). Packet103 raises the worn
chestplate damage from `0` to `2`, matching the unreduced incoming
durability cost.

This map does not claim incoming damage reduction (M451), armor crafts
(M314/M320-M322), equip-only window proofs (M270-M273), or PvP Packet7
(M66). Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52+nbt-slot102-iron-chestplate307|cause=nbt-entityid-zombie+time-14000+worn-iron-chestplate|wire=packet24-type54+packet38-status2+packet8-health+packet103-slot6|oracle=zombie-melee-armor-durability-hit-not-reduction-not-pvp-not-craft-not-equip-only|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Zombie,mob=type54,night=14000,armor=307,slot=6,before=0,after=2,hit=20->19:1,food=322+320,wire=packet24-type54+packet8+packet103,not-m451-reduction,not-craft,not-equip-only,not-m66-pvp,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a8ddc5a76726f9c1afd03c7f4dcbf222f0b8197112369ff2f8e5630a3c31b6c3`.
