<!-- worldline-map-schema=1 -->
<!-- boundary=m583-wolf-sit-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=56eb02100c063bedf32f982e6d7e66bd756ad07848f21c62443cd90b26786659 -->

# M583 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Wolf`. Packet24 type `95` is a wolf. Packet7 button 0 while
holding bone item `352` is retried until Packet38 status `7`. Vanilla taming
sits the wolf (Packet40 index 16 bit 0 plus tamed bit 2). Stick item `280`
then unsits that owner wolf. A first owner Packet7 button 0 sits it again
and a second Packet7 button 0 stands it. Sitting is the Packet40 index 16
bit 0 flag, not movement, anger, or assist.

This map does not re-qualify M420 red-collar dye `351:4`, M449 wild pack
anger, or M468 tamed assist on sheep `91`. It does not claim breeding,
sitting persistence across login, XP, or later wolf armor. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+wolf-spawner52|cause=packet7-button0-bone352+packet7-button0-stick280-sit+packet7-button0-stick280-stand|wire=packet24-type95+packet38-status7+packet40-index16-sit+packet40-index16-stand|oracle=tamed-wolf-type95-owner-sit-stand-not-m449-anger-not-m468-assist|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type95,bone=352,tame=packet38-status7,sit=packet7-button0+packet40-sit,stand=packet7-button0+packet40-stand,held=280,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`56eb02100c063bedf32f982e6d7e66bd756ad07848f21c62443cd90b26786659`.
