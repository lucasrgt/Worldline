# Worldline Semantic Catalog GO Audit

Status: **GO**

The semantic catalog does not change the public product version. Worldline
remains v0.7.0 / M9. This audit promotes the closed 24-category catalog,
coverage gate, static role graph, and CLI `role=` aliases to a stable
milestone.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Closed catalog | 24 categories and 188 required roles; unknown or duplicate symbols fail closed | PASS |
| Map coverage | Every named `symbols.map` symbol has a catalog role | PASS |
| Static graph | `SemanticGraph` fails closed on unknown read/write/dep tokens | PASS |
| Neutral CLI | `semantics show\|graph\|category\|role` inspects without loading Minecraft | PASS |
| M6 alias | Diverged `slot` prints `role=HOTBAR_SLOT` after the exact `TraceDiff` document | PASS |
| M9 order | Minimizer tries disposable lab/noise steps first; one-minimal result is unchanged | PASS |

Frozen catalog SHA-256:

```text
7b67267df9b2804b52a607ac5c7f167c857530de65c7a5e5462af8d89cfb6e10
```

Canonical qualification command:

```text
java tools/harness/Verify.java --smoke
```

The contract and non-claims are in `docs/SEMANTICS.md`.
