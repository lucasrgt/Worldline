# M618 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Wolf`. Packet24 type `95` is a wild wolf. Packet7 button 0 is
sent with bone item `352` until Packet38 status `7` (hearts), with an absolute
limit of 64 bones. Status `6` (smoke) permits another bounded attempt; the
random attempt number is not part of the semantic claim. Packet40 index 16
bit 2 is the tamed/collar flag. Persisted Wolf `Owner` NBT equals
`WolfTame618` after a clean save.

This map does not re-qualify M420 red-collar dye `351:4`, M449 wild pack
anger, M468 tamed assist, or M583 sit/stand. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+wolf-spawner52|cause=bounded-packet7-button0-bone352-until-tamed|wire=packet24-type95+packet38-status7+packet40-tamed-owner+no-packet38-status3|oracle=wolf-type95-bounded-tame-owner-collar-not-m420-dye-not-m449-anger-not-m468-assist-not-m583-sit|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type95,bone=352,bones=bounded<=64,tame=packet38-status7,collar=red,owner=WolfTame618,tamed=packet40-bit2,death=no-packet38-status3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`548375ef25ac7b5537eed78118b10d1198ec9b2958e30fd8796f7a65c8d34af2`.
