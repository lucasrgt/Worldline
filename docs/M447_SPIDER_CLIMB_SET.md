# M447 spider climb set

M447 opens the official spider-climb family. One saved mob spawner is
retargeted from `Pig` to `Spider`. After midnight (`time set 14000`) the
headless protocol-14 client observes Packet24 type `52` and
bounded non-blocking Packet31/33/34 positive-Y motion against a tall cobble
`4` wall and a tall oak plank `5` wall in one session. This is distinct
from M409 spider string `287`, from M457 spider leap/touch, and from M435
natural spawn identity.

Twenty-four fences keep selected spawns on the platform. Moving both walls one
cell inward leaves an observation lane inside that perimeter. The polling path
fails closed after fixed five-tick windows instead of blocking on one vanished
entity movement.

Frozen semantic SHA-256:
`dd9ff5fff66e6f0e70d4f4dd873aa178184ec6e7b407e99cc861cec68916f588`.

This milestone does not claim string drops, cobweb place, leap attack,
natural spawn identity, XP, or other hostile types. Headless
`B173WireClient` only. No GUI. No Aero.
