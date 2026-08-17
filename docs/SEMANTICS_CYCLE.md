# Worldline Semantic Catalog GO Audit

Status: **GO**

The semantic catalog does not change the public product version. Worldline
remains v0.7.0 / M9. This audit promotes the closed 24-category catalog,
coverage gate, static role graph, and CLI `role=` aliases to a stable
milestone.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Closed catalog | 24 categories and 196 required roles; unknown or duplicate symbols fail closed | PASS |
| Adapter manifests | `b173-client` sites cover every `worldline/b173/` catalog symbol; Aero types fail closed | PASS |
| Map coverage | Every named `symbols.map` symbol has a catalog role | PASS |
| Static graph | `SemanticGraph` fails closed on unknown read/write/dep tokens | PASS |
| Neutral CLI | `semantics show\|graph\|category\|role` inspects without loading Minecraft | PASS |
| M6 alias | Diverged `slot` prints `role=HOTBAR_SLOT` after the exact `TraceDiff` document | PASS |
| M9 order | Minimizer tries disposable lab/noise steps first; one-minimal result is unchanged | PASS |

Frozen catalog SHA-256:

```text
b4d1f4fdf968f785cc5c94b2400d5f4ad4966f8f7b042d0fd2372d24e9dadf88
```

Canonical qualification command:

```text
java tools/harness/Verify.java --smoke
```

The contract and non-claims are in `docs/SEMANTICS.md`.
