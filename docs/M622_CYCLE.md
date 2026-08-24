# M622-PATHFINDING-MATRIX Entity pathfinding matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes deterministic Beta 1.7.3 entity route construction across open, detour, and sealed terrain.

## Qualification cycle

M622 constructs the same three in-memory terrain fixtures against mapped and official Beta 1.7.3 server classes. Each run requests an EntityPig route to the same coordinate and records every path node. Open terrain must remain direct, the wall fixture must route through its gap, and the sealed target must stop outside its two-block-high ring. Two mapped and two official processes must produce the same canonical trace.

Expected signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `7d60a218116c3281ab77011768f14b4237d0b92b81f3e0d99cdb6fabb085029a`.
