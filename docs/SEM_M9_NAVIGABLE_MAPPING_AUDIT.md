# SEM-M9 Navigable Mapping Audit

Status: implementation verified with synthetic exact-identity inputs; generation
from the pinned Beta 1.7.3 queue awaits the exclusive official-runtime window.

SEM-M9 turns one exact SEM-M7 queue plus its SEM-M8 evidence into a deterministic,
self-contained HTML audit:

```text
worldline mappings html <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny> \
  <evidence.tsv> <output.html>
```

The document records both queue and evidence SHA-256 digests, totals every status,
provides client-side status filters, and gives every queue item a stable hash
anchor. Each row exposes its exact identity, side, symbol kind, descriptor,
available namespace names, evidence sources and aliases, and required next
evidence. All dynamic text is HTML-escaped. The renderer has no external assets,
network requests, timestamps, or nondeterministic ordering, so identical inputs
produce byte-identical output.

The command uses create-new output semantics and will not overwrite an existing
audit. Generated official-runtime reports remain local evidence and are not
committed.
