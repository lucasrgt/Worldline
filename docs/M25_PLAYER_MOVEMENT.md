# M25 Multiplayer Player Movement

M25 adds relative movement intent to `PlayableMultiplayerSession`. The b1.7.3
play codec retains the exact stance height received during initial
synchronization, constructs a native position/look packet, and returns the
requested neutral `PlayerPose`.

The official server spawns players at the horizontal center of a block. The
M25 oracle requests only `+0.125 X`, keeping the complete player footprint
inside that same block cell. This makes the action independent of random
adjacent terrain while still exercising native server movement validation.

Two fresh clients acknowledge position, move, disconnect, and force save. The
persisted player NBT must contain the exact target X/Y/Z in both scenarios.

## Non-claims

M25 does not interpret arbitrary collision-correction packets, simulate a
continuous movement loop, load/render chunks, run the official graphical
client, claim multiplayer determinism, or externally control server ticks.
