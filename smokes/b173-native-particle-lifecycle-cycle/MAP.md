<!-- worldline-map-schema=1 -->
<!-- boundary=client-particle-render -->
<!-- nonclaims=random-distributions,all-particle-types,world-emission-dispatch,cross-gpu-pixels -->
<!-- frozen-trace=b5bd0e3ae81ed2204066bd5930af60b9d7767af0b8f288cff29af6ce634083a9 -->

# Native particle lifecycle behavior map

The public `client-particle-render` contract binds `worldline.testkit.ParticleLifecycleFixture#verify`.
The three atoms are native construction invariants, manager aging and removal, and live/terminal frames.
Three native classes (smoke, flame, red dust) are created through their actual constructors.
Age zero, live status, layer zero and positive bounded lifetime are checked before controlled state setup.
The fixed eight-tick state controls velocity, position, scale, color and per-entity random source.
Native `EffectRenderer.addEffect`, `updateEffects` and `renderParticles` own observable behavior.
The manager contains each particle through tick eight and removes it on tick nine.
Public evidence retains every ordered tick, age, death flag, membership, position bits and RGBA hash.
Four independent processes compare mapped A/B and official A/B with strict class provenance.
Only the official JAR and its local assets supply rendering behavior; no official source is maintained.

This is a rendering package, outside the block/entity registry Functional Census. Its delta there is zero;
it adds the explicit particle boundary and three reusable acceptance atoms to the behavioral Atlas.
It excludes constructor random distributions, all particle types, world emission dispatch and cross-GPU pixels.

Frozen signature: `b5bd0e3ae81ed2204066bd5930af60b9d7767af0b8f288cff29af6ce634083a9`.

Frozen signal: `family=native-particles,subjects=3,ticks=10,processes=4,oracle=mapped-official-native-rgba`.

Frozen trace: `v1|client=official-b1.7.3|family=native-particles|subjects=3|ticks=0-9|state=controlled-age-8`.
