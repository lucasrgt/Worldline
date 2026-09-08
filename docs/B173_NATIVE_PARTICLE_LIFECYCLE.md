# B173-NATIVE-PARTICLE-LIFECYCLE-CYCLE official native particle lifecycle and frames

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

The client-particle-render behavior (WorldlineRenderBehaviors.PARTICLES) covers native constructor invariants, EffectRenderer aging and removal, and live versus terminal native RGBA frames for smoke, flame and red dust. ParticleLifecycleFixture#verify accepts an explicit family and ten ordered observations per subject, rejecting missing, duplicated, reordered, prematurely dead or visually empty live states. This rendering package adds a particle boundary outside the finite block/entity Functional Census; its existing 1310 cells are unchanged.

## Qualification cycle

Four fresh JVMs run mapped A/B and untouched official A/B. Native constructors must yield live layer-zero particles with age zero and bounded positive lifetime. An explicitly controlled scene then fixes camera orientation, lifetime to eight, scale, color, position, velocity and per-entity RNG; native EffectRenderer owns every subsequent update, draw and removal. Frames at ticks one through eight must be visible, while the post-removal tick-nine frame is empty. Tick zero records membership without drawing. The oracle checks exact age, death, membership, position bits and RGBA; provenance binds particle and manager classes to the pinned runtime. This does not claim constructor random distributions, other particle types, world emission dispatch, or cross-GPU exact pixel portability. No official assets or decompiled source are distributed.

Expected signal: `family=native-particles,subjects=3,ticks=10,processes=4,oracle=mapped-official-native-rgba`.

Frozen semantic SHA-256: `b5bd0e3ae81ed2204066bd5930af60b9d7767af0b8f288cff29af6ce634083a9`.
