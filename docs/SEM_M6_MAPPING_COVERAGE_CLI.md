# SEM-M6 Mapping Coverage CLI

Status: pinned Beta 1.7.3 report and exact audit passed under the exclusive
official-runtime lock on 2026-08-22. The report inventories 6,486 graph
symbols and has SHA-256
`2011d3df409233d0e44c779c0649be251ded37124e9de46e01b1bb223661b179`.

SEM-M6 adds one stable report surface and one fail-closed audit surface:

```text
worldline mappings report <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny>
worldline mappings audit <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <coverage.properties>
```

The report deterministically composes the exact intermediary/Nostalgia set
comparison, the RetroMCP official-identity import, namespace classifications,
the complete official client/server class-file inventory, descriptor-conflict
candidates, and gap classifications. Its SHA-256 covers the full ordered metric
body.

The audit policy must enumerate every metric plus the report digest. Missing,
extra, or changed coverage fails closed. External names remain aliases; neither
the report nor the gate promotes semantic roles or invents names for uncovered
official identities.

The synthetic CLI test builds fresh mapping and class-file JARs, proves stable
report output, accepts an exact policy, and rejects an incomplete policy. The
canonical repository gate passes. Freezing the repository's real
`b1.7.3` policy and running `Verify --runtime` remain pending while another
Worldline task owns the exclusive runtime lock.
