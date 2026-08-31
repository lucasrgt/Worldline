# Worldline Atlas

Atlas is the generated, fail-closed knowledge store for Minecraft Beta 1.7.3.
It is not a wiki, not a runtime dependency, and not a second source of truth.
Authoritative Worldline artifacts produce it: the semantic catalog, public
`WorldlineBehavior` catalog, complete Functional Census, milestone Census
deltas, adapter manifests, invariants, explicitly scoped smoke properties,
qualification receipts, frozen MAP signatures, CYCLE docs, tracked
`symbols.map` files, and provenance-bound ecosystem records.

```text
SemanticCatalog / WorldlineBehavior / Functional Census
AdapterManifest / InvariantEngine / qualification receipts
smokes/*/smoke.properties + census-delta.tsv + MAP.md
        |
        v
AtlasStore (WORLDLINE-ATLAS/1)
        |
        v
worldline atlas index|context|show|gaps|coverage|evidence
```

## Principles

- Generated over handwritten. Durable claims must appear as Atlas records.
- Evidence-backed. `VERIFIED` requires an invariant name or a smoke
  `expected.signature`.
- Fail-closed. Unknown statuses, duplicate IDs, broken refs, missing smoke
  scope, missing MAP signature freezes, and coverage units without a
  denominator fail validation without launching Minecraft.
- Explicit uncertainty. `UNKNOWN` and `OBSERVATIONAL` are legitimate.
- Agent-ready. Ranked context preserves the exact status while deriving only
  `VERIFIED`, `INFERRED`, or `UNKNOWN` certainty.
- Frozen target. Every record is scoped to `b1.7.3`.
- No false precision. Coverage is a 25-by-7 matrix of declared units with
  denominator `1`. There is no single Worldline percentage.

## Schema

Each record is `WORLDLINE-ATLAS/1` with a stable `atlas.<kind>.<token>` ID,
kind, status, artifact, scope, subject, optional boundary control class,
coverage denominator, evidence tokens, refs, and SHA-256.

Kinds: `role`, `boundary`, `invariant`, `experiment`, `scenario`, `claim`,
`subsystem`, `coverage-unit`, `hypothesis`, `field`, `loader`, `api`,
`mapping-set`, `namespace`, `ecosystem-claim`.

Statuses: `VERIFIED`, `STRONG`, `EXPERIMENTAL`, `OBSERVATIONAL`, `PARTIAL`,
`REJECTED`, `RETRACTED`, `NOT_APPLICABLE`, `UNKNOWN`,
`NATIVE_NONDETERMINISTIC`.

Catalog roles import as `STRONG` when `SemanticMapping.known()` is true. They
are never auto-promoted to `VERIFIED`. The six conservation rules import as
`VERIFIED`. Indexed smokes import as `OBSERVATIONAL`; every smoke must declare
`atlas.subsystems` and `atlas.artifact=client|server|worldline` in
`smoke.properties`. Optional `atlas.roles` and `atlas.boundaries` add exact
semantic references. Atlas never infers meaning from a milestone number.

Every public behavior materializes as its declared `atlas.scenario.<token>`.
The Functional Census currently materializes 1,320 finite cells: the completed
96-by-11 block family contributes 1,056 `atlas.claim.block-NNN.<template>`
records and the 24-by-11 persistent-entity family contributes 264
`atlas.claim.entity-NNN.<template>` records. Implicit unknown cells are records,
not omissions.
Canonical claims and milestone deltas contribute provenance to the same record;
later proof does not erase earlier evidence.

Closed hypotheses map negative knowledge: rejected schedulers, open hitch
causality, out-of-version mechanics, and in-version clusters with no GO.
Trace field aliases import as `atlas.field.*`. MAP files that declare
non-claims keep a `map-nonclaims` evidence token.

## Coverage

The versioned taxonomy groups all 25 subsystems without changing their stable
identities or historical provenance:

| Domain | Subsystems |
| --- | --- |
| `simulation` | `tick-lifecycle`, `weather`, `block-ticks`, `redstone` |
| `world` | `worldgen`, `chunks`, `lighting`, `fluids`, `saves`, `dimensions` |
| `actors` | `entities`, `tile-entities`, `mob-ai`, `player` |
| `gameplay` | `inventory`, `crafting` |
| `runtime` | `protocol`, `dedicated-server`, `rendering`, `gui`, `resources` |
| `ecosystem` | `mod-ecosystem`, `mappings`, `stationapi`, `aero` |

Every record receives derived facets for domain, subsystem, record category,
status, certainty, artifact, and applicable conformance layer. Records with no
subsystem edge remain honestly grouped under the derived `knowledge` domain.
The Gate validates the closed subsystem-to-domain mapping and every record's
facet set, so taxonomy evolves with authoritative Atlas inputs instead of a
parallel hand-maintained milestone catalog.

Dimensions: TESTABILITY, CONTROL, OBSERVABILITY, ORACLE, SEMANTIC,
REPRODUCIBILITY, DETERMINISM.

A cell is `1/1` only when an importer linked qualifying evidence. Empty cells
are `UNKNOWN` and appear in `worldline atlas gaps`. The current finite matrix
is 175/175 declared units; this is completeness of the evidence-backed
taxonomy, not a claim that the simulation's state space is finite or exhausted.

## CLI

```text
worldline atlas status
worldline atlas show <id>
worldline atlas search <term>
worldline atlas index <query>
worldline atlas context <query> [--format=json] [--budget=N] [--depth=N]
worldline atlas taxonomy
worldline atlas tags
worldline atlas gaps
worldline atlas coverage
worldline atlas evidence <id>
worldline atlas graph <id>
worldline atlas export
worldline atlas changed --since <Mn>
```

From a verified repository checkout, the canonical source launcher for these
commands is `java tools/replay/Replay.java atlas ...`. Knowledge queries do not
launch Minecraft; only the legacy seed renderer requires the verified runtime.

`atlas graph` is a derived neighborhood over catalog READS/WRITES/DEPENDS_ON,
record refs, and hypothesis controls. `atlas export` is the stable
`WORLDLINE-ATLAS-STORE/1` document for Workbench consumers. `atlas changed`
is CLI-only; it is not a Verify gate and does not fail M89.

`atlas index` ranks exact IDs, subjects, evidence, refs, normalized terms,
derived facets, and a small versioned synonym set. Queries such as `chunk`,
`domain-world`, `category-claim`, `layer-universal`, and
`surface-public-testkit` are therefore stable
agent entry points. A query composed of one complete facet token is an exact
filter; ordinary language remains ranked semantic search. `atlas context` adds bounded graph neighbors and renders
domains, subsystems, tags, evidence, and refs as human-readable text or
`WORLDLINE-ATLAS-CONTEXT/1` JSON. `atlas taxonomy` renders the hierarchy and
record totals; `atlas tags` renders the complete deterministic facet index.
Ranking, tie-breaking, depth, and budgets are deterministic; no network or
embedding service is required.

Functional Census claims also expose their automation surface. The
`surface-public-testkit`, `surface-internal-api`, and `surface-smoke-only`
facets distinguish reusable public fixtures from knowledge that still needs
promotion, so the Atlas can drive the next high-value subsystem instead of a
raw milestone count. The completed block family exposes all 1,056 claims through
the public TestKit. The entity family has 41 lifecycle, materialization, and controlled-dynamics
claims on `surface-public-testkit`, 11 remaining qualified historical proofs on
`surface-smoke-only`, and 212 explicit unknown cells. Every public entity claim is
paired with a strict fixture/evidence ledger entry; none is promoted from a smoke
by label alone. These facets evolve directly from the
canonical Functional Census rather than from a hand-maintained secondary
inventory.

## Gate synchronization

Candidate verification loads the compiled `AtlasStore` and requires the
milestone's public behavior, experiment ref, signature, and every Census-delta
claim to agree. Repository verification performs the same check once over the
entire smoke catalog. A descriptor string without a real Atlas record, a delta
without its claim, a stale signature, or a missing provenance edge fails before
qualification or release.

## Legal

Atlas must not redistribute the official JAR, original assets, decompiled
Minecraft source, or `mappings.tiny`. Public data is original Worldline
metadata, derived hashes, experiment identifiers, and evidence records.
Tracked `symbols.map` files are hashed, not copied into Atlas output.
Third-party mappings are referenced by source, coordinate, namespace, and
known license; they are never vendored into Atlas. See
[`ECOSYSTEM_MAPPINGS.md`](ECOSYSTEM_MAPPINGS.md).
