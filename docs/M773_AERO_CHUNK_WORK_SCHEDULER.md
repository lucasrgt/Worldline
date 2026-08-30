# M773-AERO-CHUNK-WORK-SCHEDULER Aero visible age and debt-aware chunk-work scheduler

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four counterbalanced pairs qualify Aero's opt-in one-rebuild-per-frame scheduler in a restored 576-machine scene, proving visible-first service, bounded debt recovery, complete backlog drainage, and hitch-rate safety.

## Qualification cycle

M773 selects real visible and hidden ChunkBuilders, continuously re-dirties visible work until an initially hidden target reaches debt eight, spins the camera while invalidating sixteen builders per frame, stops injection, requires backlog zero, and compares rebuild maxima plus 50 ms hitch rate across eight fresh clients.

Expected signal: `scene=solid-576,pairs=4,budget=1,visible-first,debt=8,spin-stress=16,drain=zero,hitch=no-regression,rebuild-max=majority-smaller`.

Frozen semantic SHA-256: `79c7e74704af7c2e1145b58b5502c0bd6c1ff52071cb0ff8b9cece9270b87386`.
