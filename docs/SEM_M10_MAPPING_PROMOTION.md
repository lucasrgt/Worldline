# SEM-M10 Mapping Promotion Gates

Status: implementation verified with synthetic exact-identity inputs. The real
Beta 1.7.3 evidence checkpoint contains 65 independently corroborated items and
is ready for one exact batch decision under the exclusive official-runtime
lock. Complete-game promotion remains correctly unavailable while the mapping
queue is non-empty.

SEM-M10 adds an exact, reviewed promotion boundary:

```text
worldline mappings promote <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny> \
  <evidence.tsv> <policy.properties>
```

The five-property policy selects `batch` or `complete-game` mode and pins the
exact SHA-256 digests of the SEM-M6 coverage report, SEM-M7 queue, and SEM-M8
evidence report. Extra, missing, or drifted properties fail closed. A `batch`
decision promotes only independently corroborated items and records their exact
item IDs, aliases, and sources; unqualified and singly supported items remain
pending. Conflicts fail the whole checkpoint, and an empty batch cannot pass.
`complete-game` additionally rejects every pending or singly supported item.

`complete-game` is a stronger, non-vacuous definition. It additionally requires:

- a non-empty graph and an empty qualification queue;
- every graph symbol to be a namespace `MATCH` and a matched RetroMCP identity;
- zero RetroMCP unmatched, side-name-difference, or missing identities;
- exact Nostalgia inventory/name equality with zero missing or extra symbols;
- exact official client and server totals for classes, fields, and methods, with
  zero missing, phantom, descriptor-conflict, or classified gap entries.

The decision records its mode, all three input digests, evidence status counts,
the complete-game result, and its own digest. This separates an evidence-ready
resolution batch from the much stronger claim that the whole game is mapped.
No generated official policy or decision is committed.
