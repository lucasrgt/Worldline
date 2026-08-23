# M517-SW-ITEM-DESPAWN-AGE Sw item despawn age

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

M517 freezes the Beta 1.7.3 `EntityItem` age boundary at 6000 ticks. Mapped
and official execution compare live, despawned, young-control, and collected
item paths, including removal from the world entity list.

The expected signature is
`fc7a206cd1e7d5bf9efa1820b8ff8537d8d24dee666ab25f9238532bf0c4a414`.
Qualify it with `java tools/harness/Gate.java --milestone
m517-sw-item-despawn-age`.

Expected signal: `oracle=MATCH,fixture=m517-sw-item-despawn-age,ticks=2,controlled=true`.

Frozen semantic SHA-256: `fc7a206cd1e7d5bf9efa1820b8ff8537d8d24dee666ab25f9238532bf0c4a414`.
