# M356 jack-o-lantern crafts

M356 opens the official jack-o-lantern-craft boundary. Personal window 0
crafts jack-o-lantern `91` from pumpkin `86` plus torch `50`. The same
cycle then places leftover pumpkin `86` and the crafted lantern `91` as a
related craft/place pair.

Packet102 take of result slot `0` is accepted only when the official
server returns jack-o-lantern `91`. Packet15 of leftover pumpkin `86` and
crafted lantern `91` on the raised-stone fixture writes `86:1` and `91:1`
from look yaw `-90`. Those cells survive a clean save plus fresh login.
The frozen signal includes ids `91` and `86`.

This milestone is distinct from M171 pumpkin place-only and M190
jack-o-lantern place-only. It does not claim snow golems, iron golems,
carving, or pumpkin stem/crop growth.

Frozen semantic SHA-256:
`b870de18f5f7c2616c607111ea332fc3f4426f8f5a3a82d713703270066ee5b1`.
