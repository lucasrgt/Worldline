# M451 behavior map

A raised `7×7` grass platform with a one-block stone rim seeds leather
`298-301`, iron `306-309`, and diamond `310-313`. One default spawner `52`
is retargeted to `Zombie`. At explicit Normal difficulty `2`, each fresh
target is approached without a damaging Packet7 poke, and its Packet24 type `54` melee produces Packet8 health
drops that shrink from unarmored `20->18` (damage 2) to leather, iron, and
diamond `20->19` (damage 1). Armor is seated through window-0 Packet102
slots `5-8`. Golden apple `322` and cooked pork `320` restore health.
Families after leather reseed player NBT instead of unequipping armor
slots `5-8`.

Removing the unused diamond-sword poke prevents a previously injured zombie
from being killed when a later reconnect observes it again. Bounded movement
now fails closed. The frozen armor-reduction trace is unchanged.

This map does not claim armor crafts (M314/M320-M322), equip-only window
proofs (M270-M273), or PvP Packet7 (M66). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52+window0-slots5-8|cause=nbt-entityid-zombie+time-14000+packet102-leather-iron-diamond|wire=packet24-type54+packet38-status2+packet8-health|oracle=zombie-melee-armor-reduction-not-pvp-not-craft-not-equip-only|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Zombie,mob=type54,night=14000,unarmored=20->18:2,leather=20->19:1,iron=20->19:1,diamond=20->19:1,strict=2>1>1>1,armor=298-301+306-309+310-313,food=322+320,wire=packet24-type54+packet8,not-m66-pvp,not-craft,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`b04b51a3cb23c8254f44a5a8fddd04c0066bb3be81e60bd7b8ffde3ae89b0897`.
