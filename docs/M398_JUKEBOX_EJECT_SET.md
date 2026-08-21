# M398 jukebox eject set

M398 opens the official compound jukebox disc-eject boundary. Two
jukebox items `84` are placed on a raised stone shelf as blocks `84:0`.
Gold disc `2256` (13) is used on the first cell and green disc `2257`
(cat) on the second. Packet61 effect `1005` plays each disc, then
Packet14 while holding gold axe `286` breaks both playing `84:1` cells.
`BlockJukeBox.onBlockRemoval` ejects each stored record as Packet21.

The frozen signal names both disc ids and Packet21 eject. This milestone
clones M334's two-disc insert and is distinct from that play-only SET
and from M178's one-cell insert.

Frozen semantic SHA-256:
`21d9a2123e3a3041573a22722d268dec75ee1d0d27d84fe0ae6f22e187f2bd8f`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
