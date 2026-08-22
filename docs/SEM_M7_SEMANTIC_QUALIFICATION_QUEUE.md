# SEM-M7 Semantic Qualification Queue

Status: implementation verified with synthetic exact-identity inputs; the pinned
Beta 1.7.3 queue awaits the exclusive official-runtime window shared with SEM-M6.

SEM-M7 adds a deterministic evidence queue:

```text
worldline mappings queue <client.jar> <server.jar> <intermediary.jar> \
  <nostalgia.jar> <retromcp.properties> <retromcp.tiny>
```

The queue is derived from the complete official bytecode audit and every
non-`MATCH` namespace finding. Rows retain the identity domain (`official` or
`intermediary`), side, kind, owner, name, descriptor, available external
aliases, and the next evidence class. They are ordered by a stable priority:

1. ambiguity and side conflicts;
2. unmapped official fields and methods;
3. external-only and missing cross-source identities;
4. name differences;
5. constructors, class initializers, members of official-only owners, and
   packaged official-only classes.

This ordering favors behaviorally useful gaps without hiding library or
synthetic-member coverage. Nostalgia and RetroMCP names remain evidence-bearing
aliases only. An external-only row explicitly requires an official identity
before alias adoption, and a name difference requires behavior or cross-version
evidence before a name choice.

The ordered queue body has a SHA-256 digest. Tests prove deterministic ordering,
complete exact official-gap enumeration, CLI routing, and a stable digest. No
official artifact or generated queue is committed.
