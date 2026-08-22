# M550 dispenser QC set

M550 opens the official dispenser quasi-connectivity SET. Packet15 places
dispenser `23:4` on a raised stone column. The Trap window accepts one
cobblestone `4:0` via Packet102. An east tower then places stone `1:0` on
the dispenser and a floor lever on that block above. The dispenser cell
is not side-levered. The rising edge ejects Packet21 cobble and the Trap
window is empty.

This is distinct from M153/M333, which attach a side lever to the support
(`69:1 -> 69:9` at `5:71:4`) and eject by adjacent power. It does not
claim arrows, buckets, TNT, multi-item RNG, piston QC, or hopper
insertion. Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.
