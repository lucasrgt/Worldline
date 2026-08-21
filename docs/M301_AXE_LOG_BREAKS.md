# M301 axe log breaks

M301 opens the official stone-axe compound log-harvest boundary. Packet15
places oak `17:0`, spruce `17:1`, and birch `17:2` on a raised stone
support. Packet16 then selects stone axe item `275`. Packet14 fully
breaks each log to air and the official server emits Packet21 stacks
`17:1:0`, `17:1:1`, and `17:1:2`.

This SET is distinct from M208/M246/M247 place-only logs and from the
single-oak M322 harvest. Vanilla stone axe on each wood type drops the
matching log item, not planks. It does not claim axe durability math.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
