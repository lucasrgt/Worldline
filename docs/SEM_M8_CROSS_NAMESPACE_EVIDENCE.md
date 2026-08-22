# SEM-M8 Cross-Namespace Evidence

Status: implementation verified with synthetic exact-identity inputs; the pinned
Beta 1.7.3 evidence report awaits the exclusive official-runtime window.

SEM-M8 adds a fail-closed evidence surface for the SEM-M7 queue:

```text
worldline mappings evidence <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <evidence.tsv>
```

The input pins the exact queue digest and addresses rows by the queue item's
SHA-256 identity. Evidence sources are explicit and may be `cross-version`,
`cross-namespace`, `behavior`, or `bytecode`. Unknown queue items, malformed
fields, and duplicate sources for one item fail closed.

Each queue item receives one status:

- `UNQUALIFIED`: no evidence;
- `SUPPORTED`: evidence exists but does not independently corroborate a name;
- `CORROBORATED`: at least two independent sources agree on one alias;
- `CONFLICT`: evidence sources propose different aliases.

The report never chooses a winner for a conflict and never promotes a lone
external alias. Its ordered evidence rows, per-item statuses, summary counts,
queue digest, and report SHA-256 form a reproducible review boundary. No
official artifact, generated queue, or generated evidence report is committed.
