# M502-SW-ENTITY-COLLISION-RESOLUTION Sw entity collision resolution

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

M502 compares mapped and official horizontal collision resolution for a close
pair of living entities and a separated control pair. Ten controlled ticks
must produce identical canonical traces in four fresh JVMs.

The expected signature is
`c3830617b51785816a20934139bfc9588dafa1412ba4c9e70e16cc7972a50dd4`.
Qualify it with `java tools/harness/Gate.java --milestone
m502-sw-entity-collision-resolution`.

Expected signal: `oracle=MATCH,fixture=m502-sw-entity-collision-resolution,ticks=10,controlled=true`.

Frozen semantic SHA-256: `c3830617b51785816a20934139bfc9588dafa1412ba4c9e70e16cc7972a50dd4`.
