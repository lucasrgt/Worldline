# M513-SW-WATER-DOWNWARD-FLOW Sw water downward flow

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

M513 compares scheduled downward water flow in identical stone-lined columns.
Mapped and official Beta 1.7.3 execution share the frozen world seed and must
produce the same canonical block trace through 60 controlled ticks.

The expected signature is
`c8cd76aa79d46ffdecd0dbabdff860b95de7b1c6cce4783dbf4ea796a9bc41ee`.
Qualify it with `java tools/harness/Gate.java --milestone
m513-sw-water-downward-flow`.

Expected signal: `oracle=MATCH,fixture=m513-sw-water-downward-flow,ticks=60,controlled=true`.

Frozen semantic SHA-256: `c8cd76aa79d46ffdecd0dbabdff860b95de7b1c6cce4783dbf4ea796a9bc41ee`.
