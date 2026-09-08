<!-- worldline-map-schema=1 -->
<!-- boundary=entity-state-conformance -->
<!-- nonclaims=exhaustive-causes,all-ai-paths,all-animation-states,cross-gpu-qualification -->
<!-- frozen-trace=0f6e2ff6cd9dfa225980f52e06816e781e4071c3ac8eb3b5077164c0a60132ad -->

# Native entity state conformance map

The public `entity-state-conformance` behavior binds `EntityStateFixture#verify` for gameplay
and `EntityVisualStateFixture#verify` for rendering. The four atoms are environmental causes,
contextual death drops, conditional target selection and body/animation/shadow/fire contrasts.
Native constructors, per-entity seeds, blocks, breathing reserve and render pose define preconditions.
Original native methods own damage, immunity, drowning, suffocation, drops and AI target choice.
Native Render dispatch owns every pixel, including isolated shadow-only and fire-only frames.

Five concrete registrations participate across the combined server/client package. The current
Functional Census already covers their broader cells: this is a depth extension with zero new
cells and no change to the 1310-cell denominator. It never substitutes these bounded observations
for the existing wider identity, interaction, motion or rendering contracts.

Eight processes compare mapped A/B and official A/B separately on each runtime side. Raw frames
and GL device metadata are retained outside the canonical same-device semantic signature.
AMD has no attached display on this host; two per-executable Windows preference probes returned
the same NVIDIA RTX 4060. Cross-device qualification is therefore explicitly pending.

Frozen signal: `family=entity-state-conformance,gameplay-states=50,render-states=14,processes=8,oracle=mapped-official-native`.

Frozen trace: `v1|family=entity-state-conformance|damage=12|drop-contexts=4x8|ai=6|render=2x7|gpu=local-device`.

The public `EntityGpuPortabilityFixture#verify` requires distinct vendor/renderer identities,
all fourteen raw RGBA frames, at least 99 percent foreground overlap, at most 0.5 percent
channels differing by more than four levels, and mean absolute channel error at most one.
`tools/integration/EntityGpuComparison.java` applies this contract offline to two retained
frame directories. Same-device repetitions and driver-version-only changes are rejected.
Native qualification on a second GPU is still required before portability can be claimed.
