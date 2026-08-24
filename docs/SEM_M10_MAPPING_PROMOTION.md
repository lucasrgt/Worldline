# SEM-M10 Mapping Promotion Gates

Status: the implementation and real Beta 1.7.3 batch were verified under the
exclusive official-runtime lock on 2026-08-23. The exact checkpoint promoted
65 independently corroborated items, retained 12,576 unqualified items, and
had zero singly supported items or conflicts. The schema-2 decision has
SHA-256 `36cde9c115f7db6665d1eaedc99e2c9e50ee5d54c01c6952caee3035924ff2ef`.
The bytecode-exhaustive diagnostic remains unavailable while the mapping queue
is non-empty. It is not the complete-game release constitution; SEM-M13 owns
that maintained dual-source boundary.

SEM-M10 adds an exact, reviewed promotion boundary:

```text
worldline mappings promote <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny> \
  <evidence.tsv> <policy.properties>
```

The five-property policy selects `batch` or `bytecode-exhaustive` mode and pins the
exact SHA-256 digests of the SEM-M6 coverage report, SEM-M7 queue, and SEM-M8
evidence report. Extra, missing, or drifted properties fail closed. A `batch`
decision promotes only independently corroborated items and records their exact
item IDs, aliases, and sources; unqualified and singly supported items remain
pending. Conflicts fail the whole checkpoint, and an empty batch cannot pass.
`bytecode-exhaustive` additionally rejects every pending or singly supported item.

`bytecode-exhaustive` is a stronger, non-vacuous diagnostic. It additionally requires:

- a non-empty graph and an empty qualification queue;
- every graph symbol to be a namespace `MATCH` and a matched RetroMCP identity;
- zero RetroMCP unmatched, side-name-difference, or missing identities;
- exact Nostalgia inventory/name equality with zero missing or extra symbols;
- exact official client and server totals for classes, fields, and methods, with
  zero missing, phantom, descriptor-conflict, or classified gap entries.

The decision records its mode, all three input digests, evidence status counts,
the bytecode-exhaustive result, and its own digest. This separates an
evidence-ready resolution batch from an intentionally stricter research audit.
No generated official policy or decision is committed.
