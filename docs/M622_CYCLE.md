# M622-PATHFINDING-MATRIX Entity pathfinding matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone exposes deterministic Beta 1.7.3 entity route construction as a public TestKit mini-subsystem across open, detour, and sealed terrain.

## Qualification cycle

M622 constructs the same three in-memory terrain fixtures against mapped and official Beta 1.7.3 server classes. Each mapped run captures immutable public route observations and delegates endpoint, obstacle-gap, and sealed-ring invariants to PathfindingMatrixFixture, producing canonical equatable evidence. The independent official oracle still records every path node, and two mapped plus two official processes must produce the same canonical trace.

Expected signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `7d60a218116c3281ab77011768f14b4237d0b92b81f3e0d99cdb6fabb085029a`.
