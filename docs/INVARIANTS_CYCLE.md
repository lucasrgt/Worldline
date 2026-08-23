# Worldline Invariant Engine GO Audit

Status: **GO**

The Invariant Engine does not change the public product version. Worldline
remains v0.7.0 / M9. This audit promotes the six fail-closed conservation
rules and their CLI field aliases to a stable milestone.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Six named rules | `standard(runtime)` runs item, entity, block, health, durability, and time | PASS |
| Live watch | Controlled-client 16-tick cycle attaches `standard(runtime)` on the mapped client | PASS |
| Fail closed | Unit suite rejects unexplained item, block, entity, heal, repair, and rewound time | PASS |
| CLI alias | Diverged `block65` prints `invariant=block-conservation` after the M6 document | PASS |
| Fingerprint safety | `TraceDiff.render()` is unchanged so M9 fingerprints stay stable | PASS |

The live-watch oracle is the frozen 16-tick client state signature:

```text
e8cdeba39a44b772a70c48c0acd9ae3983f3d95a8c10c545df5d66fb953db554
```

Canonical qualification command:

```text
java tools/harness/Gate.java --smoke
```

The contract and non-claims are in `docs/INVARIANTS.md`.
