# M504-M508-SW-ENTITY-DYNAMICS Sw entity dynamics

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

This grouped differential covers target-free ghast drift, slime jumping,
boat travel and collision, and minecart rail motion and braking. Each boundary
uses a deterministic memory-world fixture and compares mapped and official
Beta 1.7.3 execution over 244 controlled ticks.

The expected signature is
`adf4090e04b92ccc22a6adf5a7bd8892fe6ecdd4e6b8c4ba21e2f201962f71db`.
Qualify it with `java tools/harness/Gate.java --milestone
m504-m508-sw-entity-dynamics`.

Expected signal: `oracle=MATCH,fixture=m504-m508-sw-entity-dynamics,ticks=244,controlled=true`.

Frozen semantic SHA-256: `adf4090e04b92ccc22a6adf5a7bd8892fe6ecdd4e6b8c4ba21e2f201962f71db`.
