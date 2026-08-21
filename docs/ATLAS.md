# Worldline Atlas

Atlas is the generated, fail-closed knowledge store for Minecraft Beta 1.7.3.
It is not a wiki, not a runtime dependency, and not a second source of truth.
Authoritative Worldline artifacts produce it: the semantic catalog, adapter
manifests, invariants, smoke properties, MAP SHA-256 lines, CYCLE docs, and
tracked `symbols.map` files.

```text
SemanticCatalog / AdapterManifest / InvariantEngine
smokes/*/smoke.properties + MAP.md + docs/M*_CYCLE.md
        |
        v
AtlasStore (WORLDLINE-ATLAS/1)
        |
        v
worldline atlas status|show|search|gaps|coverage|evidence
```

## Principles

- Generated over handwritten. Durable claims must appear as Atlas records.
- Evidence-backed. `VERIFIED` requires an invariant name or a smoke
  `expected.signature`.
- Fail-closed. Unknown statuses, duplicate IDs, broken refs, MAP/signature
  mismatch, and coverage units without a denominator fail validation without
  launching Minecraft.
- Explicit uncertainty. `UNKNOWN` and `OBSERVATIONAL` are legitimate.
- Frozen target. Every record is scoped to `b1.7.3`.
- No false precision. Coverage is a 22-by-7 matrix of declared units with
  denominator `1`. There is no single Worldline percentage.

## Schema

Each record is `WORLDLINE-ATLAS/1` with a stable `atlas.<kind>.<token>` ID,
kind, status, artifact, scope, subject, optional boundary control class,
coverage denominator, evidence tokens, refs, and SHA-256.

Kinds: `role`, `boundary`, `invariant`, `experiment`, `scenario`,
`subsystem`, `coverage-unit`, `hypothesis`, `field`.

Statuses: `VERIFIED`, `STRONG`, `EXPERIMENTAL`, `OBSERVATIONAL`, `REJECTED`,
`UNKNOWN`, `NATIVE_NONDETERMINISTIC`.

Catalog roles import as `STRONG` when `SemanticMapping.known()` is true. They
are never auto-promoted to `VERIFIED`. The six conservation rules import as
`VERIFIED`. Indexed smokes import as `OBSERVATIONAL`; Aero M68-M88 rows are
descriptive evidence, not causal hitch claims.

Closed hypotheses map negative knowledge: rejected schedulers, open hitch
causality, out-of-version mechanics, and in-version clusters with no GO.
Trace field aliases import as `atlas.field.*`. MAP files that declare
non-claims keep a `map-nonclaims` evidence token.

## Coverage

Subsystems: tick-lifecycle, worldgen, chunks, lighting, weather, block-ticks,
fluids, entities, mob-ai, player, inventory, crafting, redstone, saves,
dimensions, protocol, dedicated-server, rendering, gui, resources, stationapi,
aero.

Dimensions: TESTABILITY, CONTROL, OBSERVABILITY, ORACLE, SEMANTIC,
REPRODUCIBILITY, DETERMINISM.

A cell is `1/1` only when an importer linked qualifying evidence. Empty cells
are `UNKNOWN` and appear in `worldline atlas gaps`.

## CLI

```text
worldline atlas status
worldline atlas show <id>
worldline atlas search <term>
worldline atlas gaps
worldline atlas coverage
worldline atlas evidence <id>
worldline atlas graph <id>
worldline atlas export
worldline atlas changed --since <Mn>
```

`atlas graph` is a derived neighborhood over catalog READS/WRITES/DEPENDS_ON,
record refs, and hypothesis controls. `atlas export` is the stable
`WORLDLINE-ATLAS-STORE/1` document for Workbench consumers. `atlas changed`
is CLI-only; it is not a Verify gate and does not fail M89.

## Legal

Atlas must not redistribute the official JAR, original assets, decompiled
Minecraft source, or `mappings.tiny`. Public data is original Worldline
metadata, derived hashes, experiment identifiers, and evidence records.
Tracked `symbols.map` files are hashed, not copied into Atlas output.
