# M420 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Wolf`. Packet24 type `95` is a wolf. Packet7 button 0 while
holding bone item `352` is retried until Packet38 status `7` (hearts). Failed
attempts are Packet38 status `6` (smoke). The tamed wolf wears the official
red collar. Packet7 with lapis dye `351:4` is then used on that living wolf.

This map does not re-qualify M388 skeleton arrows `262` or M406 sheep dye
wool. It does not claim XP, breeding, sitting persistence, or later wolf
armor.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+wolf-spawner52|cause=packet7-button0-bone352+packet7-dye351:4|wire=packet24-type95+packet38-status7+no-packet38-status3|oracle=wolf-type95-bone352-tame-red-collar-not-m406-sheep-dye-not-m388-arrows|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type95,bone=352,tame=packet38-status7,collar=red,dye=351:4+packet7,death=no-packet38-status3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`8268a761729c8e58ce515e8c1abb5065fa4782f824f83d9f2e6072f6e46d1833`.
