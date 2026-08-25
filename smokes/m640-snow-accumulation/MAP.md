<!-- worldline-map-schema=1 -->
<!-- boundary=snow-accumulation -->
<!-- nonclaims=client-precipitation-rendering,snow-depth-growth,arbitrary-biomes,successful-rng-pass -->
<!-- frozen-trace=0cb127b2882a28afd853153529bef8a454249322991371c7da3af598f97c1e7c -->

# M640 semantic map

Evidence: official-server dual replica.

The public boundary is `worldline.testkit.SnowAccumulationFixture#verify`. It advances paired
snowfall and dry-control ambient passes, requires a snow-enabled biome and block light below ten,
accepts a metadata-zero snow layer only while raining, and requires the dry control to remain air.
Evidence retains the attempt ceiling but not the successful draw or coordinate.

The mapped path invokes `World.doRandomUpdateTicks()` after native weather-strength priming. The
official path invokes the corresponding protected `dj.j()` after `dj.i()` weather updates. Both
exercise vanilla random selection, biome lookup, top-solid height, placement eligibility, rain
state, and block replacement; neither places snow directly after initialization.

Frozen signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `0cb127b2882a28afd853153529bef8a454249322991371c7da3af598f97c1e7c`.
