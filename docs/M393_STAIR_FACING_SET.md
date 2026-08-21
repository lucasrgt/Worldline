# M393 stair facing set

M393 opens the official compound oak-stairs and cobble-stairs facing set.
Packet15 of wooden stairs item `53` on a raised stone support places
`53:0` from look yaw `-90` and `53:1` from look yaw `90`. Packet15 of
cobble stairs item `67` places `67:0` and `67:1` from the same two yaws.
The frozen signal includes both `53` and `67` plus multiple facing metas.
All four cells survive a clean save plus fresh login.

This is distinct from shipping M186 and M187 (one east facing each) and
from shipping M319 (workbench crafts, no placement facing). It does not
claim inverted stairs, the remaining north/south yaws as extra hashed
variants, or slab placement. Headless `B173WireClient` only. No GUI.
No Aero.

The frozen semantic SHA-256 is
`1e94922033cceeec477b29842f80b9bce86737cb240b266bad8ad4cf93cf0253`.
