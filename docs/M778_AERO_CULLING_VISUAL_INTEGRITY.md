# M778-AERO-CULLING-VISUAL-INTEGRITY Aero culling visual integrity

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Two fresh counterbalanced sessions toggle Aero broad conservative cone culling disabled and enabled within the same client state while keeping the unsafe strict block-entity layer disabled, requiring exact phase-matched pixels over twelve frozen-world camera checkpoints and lower at-rest render work in both sessions.

## Qualification cycle

M778 restores one 120-model four-panel scene inside a static opaque enclosure per session, prewarms the shared static model, freezes world ticks, time, weather, entities, clouds, HUD, and tick delta, alternates off/on order within each client, holds eight central and four near-panel views for twenty frames each, compares matching temporal phases across opposite-order sessions, then evaluates every RGBA pixel plus per-frame Aero draw-work and view-cull counters.

Expected signal: `scene=panels120-static-mega+static-enclosure+prewarmed,sessions=2-counterbalanced,cameras=center8x45+near4x90,world=frozen-no-clouds,culling=broad-cone-safe,pixels=exact24of24-phase-matched,work=reduced2of2`.

Frozen semantic SHA-256: `a7473c0e9e1992edf4dc77c15ea1ab929c5876768b668b2be84a05ef559f674a`.
