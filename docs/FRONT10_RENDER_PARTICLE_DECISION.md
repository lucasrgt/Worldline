# FRONT-10 client render and particle decision

Worldline publishes a **bounded official claim** for smoke, flame and red dust particles
through `b173-native-particle-lifecycle-cycle`. The official Beta 1.7.3 client is the oracle.

Native constructors establish live age-zero layer-zero particles and a positive bounded lifetime.
The controlled scene fixes camera orientation, lifetime, scale, color, position, velocity and entity RNG.
`EffectRenderer.updateEffects` then owns motion, aging and removal; native `renderParticles`
produces each live frame and the empty frame after removal. Two fresh mapped processes and two
fresh untouched official processes must produce identical lifecycle observations and RGBA evidence.

The reusable TestKit contract is `worldline.testkit.ParticleLifecycleFixture#verify`.
It verifies an explicit ordered family, ten ticks per subject, exact age and manager membership,
visible live frames and empty terminal output. Evidence retains position bits and every frame hash.
The semantic anchors `EFFECT_TICK` and `TEXTURE_EFFECT_TICK` remain independently enforced.

This replaces the previous unqualified pilot decision. It does not qualify world particle emission
causes, every particle class, constructor random distributions, or exact pixel portability across GPUs.
Aero block-model and frame-census evidence remains separate from the native particle oracle.
The rendering package has no delta in the finite block/entity registry Functional Census; it adds
three particle acceptance atoms and one public Atlas behavior boundary.

The machine-readable policy is `quality/client-render-particle-boundary.properties`.
The gate requires the frozen native capture and public equatable contract, and rejects a private
Minecraft binding. Full details and the frozen signature are in `B173_NATIVE_PARTICLE_LIFECYCLE.md`.
