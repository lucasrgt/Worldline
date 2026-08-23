<!-- worldline-map-schema=1 -->
<!-- boundary=trace-divergence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=fc5baa700a2da8bedff3a17aff5647b17ae831acd2dbb839bb767229538c4436 -->

# M6 Trace Explorer Evidence Map

## Claim

Worldline v0.4.0 renders a real canonical state trace and identifies the exact
first field divergence between two executions using a runtime-independent CLI.

## Scenario

Fresh mapped-client and official-JAR processes independently execute the
controlled 16-tick scenario. Their 17-record `v2` traces must compare equal.
The viewer must expose the seed, canonical signature, full schema, record count,
and rows through `tick16`.

The runner copies the official trace and changes only `tick9.slot` from 2 to 4.
Forward comparison must report record index 9, label `tick9`, field index 11,
field `slot`, left 2, right 4, and catalog `role=HOTBAR_SLOT`. Reverse
comparison must retain the location and reverse the values. A second copy with
duplicate `clientTick` schema fields must fail strict parsing.

Frozen SHA-256 of the exact forward divergence report:

```text
fc5baa700a2da8bedff3a17aff5647b17ae831acd2dbb839bb767229538c4436
```

## Boundary

The subject and oracle execution paths remain independent. M6 receives their
canonical text only after execution; the analysis module has no game, adapter,
RetroMCP, LWJGL, or reproduction dependency. Injected divergence is isolated to
one copied trace value and never changes runtime behavior.

## Non-claims

This evidence proves exact first divergence for ordered `v2` records and long
fields. It does not prove causal attribution, semantic field interpretation,
record alignment, floating-point display, live streaming, charting, or `v1`
analysis.
