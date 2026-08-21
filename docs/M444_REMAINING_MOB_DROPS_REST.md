# M444 remaining mob drops rest

M444 opens the official remaining death-drop family not hashed by M388,
M389, M409, or M411. Two default spawners `52` are placed; the first
saved `EntityId` is rewritten from `Pig` to `Sheep` and the second stays
`Pig`. Packet24 type `90` dies to Packet7 diamond sword `276` and drops
Packet21 pork `319`. Packet24 type `91` dies the same way and drops
Packet21 undyed wool `35:0`. Both remaining drops share one frozen SET.

This is distinct from shipping M150 pork `319` as a single pig drop and
from M316/M406 living-sheep shears. It does not claim M388 zombie feather
`288` (Beta 1.7.3 zombies do not drop rotten flesh), M389 cow/chicken
drops, M409 spider string, or M411 cooked pork `320`. Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`4f0cf6fc97f045251947014072b407aae095b6419fb3c3ab94c50722f7db8f66`.
