# M438 remaining clock map set

M438 opens the official remaining clock-and-empty-map family. Packet102
on workbench `58` crafts clock `347` from four gold ingots `266` around
redstone `331`. Packet16 holds that crafted clock. Packet15 air-use of
seeded empty map `358` (direction `255` at `-1,255,-1`) is the remaining
empty-map probe. The official Beta 1.7.3 dedicated server does not fill
that stack on protocol-14: `358:1:0 -> 358:1:0`. Both the held clock and
the unfilled map survive a clean save plus fresh login.

This is distinct from M325 (workbench crafts of compass `345`, clock
`347`, and empty map `358` without hold or air-use), from M365 (seeded
compass needle), and from M366 (map-fill-only air-use). This freeze does
not invent a filled-map damage, Packet131 map data, or clock GUI.

Frozen semantic SHA-256:
`9ebe2cca746ab29d741407b8788d0b10a7e942cd691b868eb0d1d2f00e83eb58`.

Headless `B173WireClient` only. No GUI. No Aero.
