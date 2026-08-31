<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=53e03199b40fde69855920225fc782159e559bb824a92b2092067290aa1a9fe8 -->

# M780-AERO-SMOOTH-LIGHT-RESOLVED-CACHE behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's full-frame oracle. It does not claim arbitrary GPUs, dynamic model
geometry, arbitrary TTLs, or unrelated renderer paths.

Fixture: one restored world contains 128 static MegaCrusher block entities in
four panels spanning multiple chunks. Ticks, time, weather, entities, clouds,
HUD, and tick interpolation are controlled. The runtime mixin redirects only
the fixture's MegaCrusher renderer through Aero's production smooth-light path.
A scoped synthetic brightness grid supplies stable, spatially varied samples
only while that renderer calls the vanilla world-brightness API.

Actions: four fresh JVMs run in ABBA order with the smooth-light cache fixed at
startup: off, on, on, off. The on arm uses the documented default 50 ms TTL and
1,024-entry LRU capacity. Every client traverses 480 unmeasured warm frames and
240 measured frames covering orbit, traversal, fast spin, teleport, and close
inspection. At frame 120, a diagnostic captures phase-zero light at a fixed
camera, the brightness grid changes, all clients render through a 100 ms
convergence interval, and a second diagnostic captures phase-one light at the
same camera.

Observations: 24 route RGBA checkpoints plus two light diagnostics per JVM
produce two off/on comparisons and independent off/off and on/on repeatability
pairs. Same-arm pairs form the only raster-noise mask. Every pair and the union
mask are limited to 10 changed locations per million pixels; all off/on changes
outside that mask are forbidden. Before and after hashes must differ within
each run, and on arms must report hits, cold misses, and stale misses without
size mismatches or LRU evictions. Render-call work must repeat, world-light
samples and render time must decrease in both rounds and to at most 70 and 95
percent in aggregate, respectively. The cycle also records 50 ms hitches and
forbids catastrophic 1.5 second frames.

Claim: `scene=128-static-multichunk,jvms=4-fresh-abba,route=orbit+traverse+spin+teleport,light=phase-change+ttl-convergence,pixels=0-unexplained+noise<=10ppm,samples+render=reduced2of2,cache=hits+misses+cold+stale-censused,hitches=censused`.

Frozen trace: `v1|scene=128-static-megacrushers+four-multichunk-panels|jvms=4-fresh-abba-off+on+on+off|route=240-orbit+traverse+spin+teleport|warm=480-route-frames|light=synthetic-grid+phase-change+100ms-convergence|cache=immutable-startup+ttl50ms+lru1024+native-hit-miss-cold-stale-eviction-counters|captures=24-route+2-light-diagnostics-per-jvm|world=frozen+clear-weather+no-clouds|oracle=full-rgba+same-arm-noise10ppm+no-unexplained+light-change+sample-reduction+render-time-reduction+hitch-census`.

SHA-256: `53e03199b40fde69855920225fc782159e559bb824a92b2092067290aa1a9fe8`.
