# FRONT-10 client render and particle decision

Worldline explicitly does **not** claim vanilla client particle rendering in
the current release line. This is a registered boundary, not an untracked gap.

## Evidence boundary

The maintained semantic map identifies `EffectRenderer.updateEffects` as
`EFFECT_TICK` and the texture-effect update path as `TEXTURE_EFFECT_TICK`.
Those bytecode-anchored identities prove where the client behavior lives; they
do not prove particle creation, lifetime, motion, batching, color, texture,
camera-facing geometry, or final pixels.

Existing Aero evidence is narrower. It proves block-model content reaches the
renderer and freezes frame/stage census and timing observations. It does not
use Aero's instrumentation as a substitute oracle for vanilla particles and it
does not generalize block-model observations to `EffectRenderer`.

## Decision

FRONT-10 selects the explicit non-claim route. A one-particle pilot is deferred
until it can satisfy all of these conditions in one reviewed milestone:

- an official Beta 1.7.3 client is the behavioral oracle;
- one deterministic particle cause is isolated from ambient effects;
- two fresh replicas agree on creation, bounded lifetime, and final observable
  evidence;
- the observation is exposed through a reusable TestKit contract rather than a
  smoke-local or decompiled-source assertion;
- client/GUI lane identity, official-runtime locking, and content-addressed
  qualification evidence remain intact.

The machine-readable decision is
`quality/client-render-particle-boundary.properties`. The canonical Gate fails
if the file, this document, the semantic anchors, or the absence of a qualified
client-particle behavior drifts. Adding a real pilot therefore requires an
explicit constitution update instead of silently broadening existing claims.
