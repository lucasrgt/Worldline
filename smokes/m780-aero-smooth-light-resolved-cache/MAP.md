<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=96a4ffd9d0574aead20652e1739202e5102ba2a0e2f5b4955915892fadc9154e -->

# M780-AERO-SMOOTH-LIGHT-RESOLVED-CACHE behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's full-frame oracle. It does not claim arbitrary GPUs, dynamic model
geometry, arbitrary TTLs, or unrelated renderer paths.

Fixture: one restored world contains 128 controlled block entities in four
panels spanning multiple chunks. Each renders a 2,048-triangle smooth grid
whose cells never overlap, removing model z-fighting from the visual oracle.
The fixture emits without texturing or blending, so iteration order cannot
change transparent material composition.
The fixture also sorts block entities by coordinates before capture so draw
order is identical after every fresh deserialization.
Ticks, time, weather, entities, clouds, HUD, and interpolation are controlled.
The runtime mixin redirects only the fixture renderer through Aero's
production smooth-light path. Immediately before the first controlled mesh,
the oracle clears color and depth so terrain and water cannot contaminate the
subject framebuffer.
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
samples must decrease in both rounds and to at most 70 percent in aggregate.
Promotion additionally requires render time to decrease in both rounds and to
at most 95 percent in aggregate without material hitch regression; otherwise
the evidence explicitly keeps the candidate disabled. The cycle forbids
catastrophic 1.5 second frames.

Claim: `scene=128-static-multichunk,jvms=4-fresh-abba,route=orbit+traverse+spin+teleport,light=phase-change+ttl-convergence,pixels=0-unexplained+noise<=10ppm,samples=reduced2of2,cache=hits+misses+cold+stale-censused,decision=promote-or-keep-disabled-by-render+hitches`.

Frozen trace: `v6|scene=128-dense-smooth-grid-2048tri+four-panels+opaque-buffer+stable-be-order|jvms=4-fresh-abba-off+on+on+off|route=240-orbit+traverse+spin+teleport|warm=480-route-frames|light=synthetic-grid+phase-change+100ms-convergence|cache=immutable-startup+ttl50ms+lru1024+native-hit-miss-cold-stale-eviction-counters|captures=24-route+2-light-diagnostics-per-jvm|world=frozen+clear-weather+no-clouds|oracle=full-rgba+same-arm-noise10ppm+no-unexplained+light-change+sample-reduction+render-time-reduction+hitch-census`.

SHA-256: `96a4ffd9d0574aead20652e1739202e5102ba2a0e2f5b4955915892fadc9154e`.
