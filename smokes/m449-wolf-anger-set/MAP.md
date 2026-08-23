# M449 behavior map

The fixture raises an isolated `7×7` grass platform two blocks above the
water column, closes it with 24 fence blocks `85:0`, and places one default
mob spawner `52:0`. After a clean save
the region NBT `EntityId` is rewritten from `Pig` to `Wolf`. Packet24 type
`95` is a wolf. Packet7 button 1 while holding wood sword `268` (no bone
`352`) nonlethally strikes an arena-contained wild wolf. `B173MobTracker.takeTame` stays `-1`, so
Packet38 is not tame status `6` or `7`. Packet8 then reports player health
loss from wolf hostility. The struck wolf remains living (no Packet38 status `3`).

This map does not re-qualify M420 bone tame plus dye collar. It does not
claim breeding, sitting persistence, which nearby wolf caused Packet8, or later wolf armor.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-arena+wolf-spawner52|cause=packet7-button1-wood-sword268-no-bone|wire=packet24-type95+packet38-not-status6-or-7+packet8-health|oracle=wolf-type95-nonlethal-strike-hostility-not-tame-not-breeding-not-sitting|column=11,platform=7x7-48grass,arena=fence85-24,spawner=4:66:4:52:0,mob=type95,sword=268,held=no-bone,tame=no-packet38-status6-or-7,hostile=packet8-health,death=no-packet38-status3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`c459a0789fdb7cd773db7301dbc0e66db10b335a87a017a95d0853817a743057`.
