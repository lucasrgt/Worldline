<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=dd3575df61a9f9ab10a054b9a40f4547375a9280368ce40799de5d2f55f97a84 -->

# M779-AERO-ANIMATED-VISUAL-INTEGRITY behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's full-frame oracle. It does not claim arbitrary GPUs, every model
format, the unsafe strict block-entity view culler, or occlusion-query culling.

Fixture: one restored world with 120 block entities split evenly between
keyframed MegaCrusher, MorphCrystal, and player-targeted TurretIK models. Four
panels use six-block spacing to prevent coplanar animated-model intersections
and live inside an opaque static enclosure with frozen ticks,
time 6000, clear weather, no ambient entities, no client-clock clouds, hidden
HUD, fixed tick delta, and all production batch reuses plus the
animated-to-at-rest LOD retained.

Actions: four fresh JVMs run in ABBA order: cull-off, cull-on, cull-on,
cull-off. The broad conservative cone-culling flag is immutable from process
startup, so no final static field is mutated after class loading. Every client
pre-traverses the complete route five times for 1,200 unmeasured frames so
visited render chunks and model paths are resident under its one immutable arm.
The pinned Aero runtime supplies stable model-resource-name batch ordering and
tests each model's visual-radius sphere, rather than only its origin, against
the broad cone.
Every client executes 240 measured frames spanning orbit, walk, fast spin,
teleport, and a near-panel orbit.
Every model is sought to a deterministic spatially varied keyframe or morph
pose each frame; IK uses the deterministic player route as its target. Previous
and last-tick camera fields are collapsed onto each current sample so runtime
frame timing cannot alter interpolation while the sample positions still move.

Observations: 24 complete RGBA checkpoints per JVM create 96 comparisons:
24 off/on pairs in each of two rounds, 24 off/off repeatability pairs, and 24
on/on repeatability pairs. The same-arm pairs define an independent raster
noise mask. Every pair and the union mask are limited to 10 changed locations
per million observed pixels; every off/on change outside the mask is forbidden.
The cycle also records a stable signature of the radius-aware cone membership
at every capture. The cycle records both
animated and at-rest execution, measures render duration and 50 ms hitches,
requires animated, at-rest, and list-call work to repeat exactly within each
arm, forbids catastrophic 1.5 second frames, and requires animated work and
total render time to fall in both rounds and to at most 95 percent in aggregate.

Claim: `scene=120-dynamic-40keyframed+40morph+40ik,jvms=4-fresh-abba,route=orbit+walk+fast-spin+teleport+near-orbit,lod=animated+at-rest-transition,frames=960,culling=immutable-startup-radius-aware-broad-cone,pixels=0-unexplained+raster-noise<=10ppm,work+render=reduced2of2,hitches=censused`.

Frozen trace: `v14|scene=120-dynamic-40-keyframed+40-morph+40-ik+nonoverlap-panels+static-enclosure+prewarmed|jvms=4-fresh-abba-off+on+on+off|route=240-frames-orbit+walk+fast-spin+teleport+near-orbit|camera=continuous-samples+collapsed-interpolation-history|poses=deterministic-per-frame+spatial-phase|cache-history=per-jvm-full-route-warm1200|batch-order=stable-model-name|cone-bounds=radius-aware-sphere|reuse=production-defaults|captures=24-per-jvm|visibility=per-checkpoint-radius-signature|world=frozen-tick+time6000+clear-weather+no-entities+no-clouds|contrast=immutable-startup-frustum-off-vs-broad-on+be-view-off+production-animation-lod|oracle=full-rgba-differential-no-unexplained+same-arm-noise10ppm+same-arm-work-repeatable+animated-work-reduction+render-time-reduction+hitch-census`.

SHA-256: `dd3575df61a9f9ab10a054b9a40f4547375a9280368ce40799de5d2f55f97a84`.
