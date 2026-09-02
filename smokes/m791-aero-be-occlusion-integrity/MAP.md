<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-render-occlusion -->
<!-- nonclaims=no-triangle-level-or-arbitrary-gpu-claim -->
<!-- frozen-trace=6c5581ab8b006dd2b4b28e8c9405d275d3f779b08c1dcf0bd8568cf1fb8fefd5 -->

# M791 Aero block-entity cell occlusion behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's Extensions SDK discovery and full-frame runtime oracle. The SDK
entrypoint stays in the external project and imports only public Worldline
authoring packages. Worldline owns the camera, framebuffer, process, and
counterbalanced acquisition plan.

Fixture: 512 page-capable MegaCrusher instances, half static and half animated,
occupy two 256-machine vertical layers across four chunks. Opaque slabs at Y 79
and 80 separate the two vanilla 16-block render sections. World time, weather,
entities, clouds, HUD, tick delta, and animation ticks are frozen. Advanced
OpenGL is enabled so completed vanilla occlusion-query state exists.

Actions: two fresh sessions execute opposite off/on orders. Each arm holds
eight 45-degree cameras beneath the slab and four 90-degree cameras after a
vertical teleport above it. The candidate history is cleared at every arm
boundary and receives twenty frames to stabilize and reveal.

Observations: 24 phase-matched off/on pairs compare every RGBA pixel. Renderer
evidence includes static submissions, Cell Page calls, animated queues,
candidate rejections, and stable occluded slices. Candidate-off rejections
must remain zero. Candidate-on rejections and stable slices must be non-zero,
submitted work must fall in both sessions, and aggregate work may not exceed
80 percent of rollback.

Claim: `scene=4chunks-512-mixed-2layers+double-slab,sdk=discovered,sessions=2-counterbalanced,cameras=covered8x45+reveal4x90,pixels=exact24of24,occlusion=activated2of2,work=reduced2of2`.

Frozen trace: `v1|extension=ultra-journey-sdk-v1|scene=4chunks-512-mixed-2layers+double-slab|sessions=2|orders=off-on+on-off|cameras=covered8x45+reveal4x90|hold=20|advanced-opengl=on|world=frozen-tick+time6000+clear-weather+no-entities+no-clouds|contrast=be-cell-occlusion-off-vs-on|other-culls=off|oracle=exact-rgba+activation+submission-reduction|captures=12-per-arm`.

SHA-256: `6c5581ab8b006dd2b4b28e8c9405d275d3f779b08c1dcf0bd8568cf1fb8fefd5`.
