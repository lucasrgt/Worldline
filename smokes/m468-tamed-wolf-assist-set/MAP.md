# M468 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the first region NBT `EntityId` is
rewritten from `Pig` to `Sheep` and the remaining `Pig` becomes `Wolf`.
Packet24 type `95` is tamed with bone item `352` via Packet7 button 0 until
Packet38 status `7`. Pending type `91` spawns from the hunt window are
discarded so a fresh sheep can appear while the wolf is sitting. The owner
then unsits that wolf with Packet7 button 0 while holding diamond sword
`276` and strikes the sheep with Packet7 button 1. The tamed wolf must
finish that living sheep: Packet38 status `2` is observed on the target,
and Packet38 status `3` plus Packet29 occur without a second player hit.

This map does not re-qualify M420 red-collar dye `351:4`, M449 wild pack
anger, M406 sheep dye wool, or breeding. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+wolf-spawner52+sheep-spawner52|cause=packet7-button0-bone352+packet7-button0-unsit+one-packet7-button1-sword276|wire=packet24-type95+packet24-type91+packet38-status7+packet38-status2+packet38-status3-not-player-only|oracle=tamed-wolf-type95-assist-sheep91-not-m420-collar-dye-not-m449-pack-anger|column=17,platform=7x7-48grass,wolf=4:72:4:52:0,sheep=5:72:4:52:0,mobs=type95+type91,bone=352,tame=packet38-status7,unsit=packet7-button0,sword=276,player-hits=1,hurt=packet38-status2,assist=packet38-status3-not-player-only,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`21920ae6ac95c99bc80e2adfef34dd66a8649b88c45836f5e61119f5fee019d6`.
