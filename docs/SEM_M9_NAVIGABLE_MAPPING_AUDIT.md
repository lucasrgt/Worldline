# SEM-M9 Navigable Mapping Audit

Status: the self-contained pinned Beta 1.7.3 audit was generated from the exact
12,641-item queue and empty evidence checkpoint under the exclusive
official-runtime lock on 2026-08-22. Its queue SHA-256 is
`a8be4aed7b68a99b2b586908feb5b638b269f3bfdef9f41a9f5c82d050798d54`.

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
