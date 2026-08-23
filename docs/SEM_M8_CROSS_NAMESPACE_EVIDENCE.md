# SEM-M8 Cross-Namespace Evidence

Status: pinned Beta 1.7.3 evidence report generated under the exclusive
official-runtime lock on 2026-08-22. The deliberately empty evidence input
left all 12,641 items unqualified, with zero supported, corroborated, or
conflicting aliases. Report SHA-256:
`ea7a89680232ec6ada804fb8f62d1f9343e5f58c5d902ab0b46dee5091036dc3`.

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
