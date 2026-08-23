# Fable 2 Total-Program Ledger

This ledger is the release contract for the second Fable review accepted on
2026-08-23. Every report item is represented below. A row becomes `DONE` only
when implementation, automated enforcement, documentation, and current-state
evidence agree. `EXTERNAL` is permitted only for an unavoidable operator or
third-party action with a passing fail-closed doctor and an exact instruction.

Status values are `DONE`, `ACTIVE`, `QUEUED`, and `EXTERNAL`.

## Evidence integrity and incident prevention

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| FIX-01 | DONE | Make portable smoke-pin restoration fail closed on real evidence bytes. | Schema 5 has 525 tracked envelopes; `SmokeReceiptCacheTest` rejects corrupt and missing bytes, and the canonical Gate passed. |
| FIX-02 | DONE | Terminate complete delegated runtime trees after the 24-hour fabric or lease timeout. | Runtime Fabric and the official lease kill reverse-order descendants, confirm death, and pass the parent/child termination probe. |
| FIX-03 | DONE | Prevent suite PASS receipts from permanently masking platform flakes. | Suite fingerprint v2 binds OS/architecture; the runtime workflow deterministically re-executes 10% by run ID and cache tests cover sampling limits. |
| FIX-04 | DONE | Harden FIFO lease tickets against partial writes and PID reuse. | Atomic ticket publication, bounded partial-read retries, PID/start-time identity, and three pruning cases pass `FairFileLeaseTest`. |
| FIX-05 | DONE | Make immutable module-cache publication and garbage collection junction-safe. | Publication never replaces a digest directory; usage GC locks entries, protects all worktree links, enforces age/size, and its doctor reports 97 valid entries/22 references. |
| FIX-06 | DONE | Confirm process death and use reverse descendant order everywhere. | `ProcessCapture` and host/runtime coordinators wait for confirmed tree death; Runtime Fabric self-tests pass. |
| FIX-07 | DONE | Bound every GitHub Actions job. | Every job in all five workflows declares a reviewed `timeout-minutes`, including the 1,440-minute official-runtime ceiling. |
| FIX-08 | DONE | Remove the remaining regex/legacy parsing debt. | `TokeiReport` uses strict structured JSON in repository/candidate checks; `SmokeLegacyImport` and its active workflow contract are removed. |

## Gate latency

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| LAT-01 | DONE | Cache integration-tool compilation and self-tests by content digest. | The 15.5-second stage restores a platform-bound `verification-stages/integration-tools` receipt on an unchanged input set. |
| LAT-02 | DONE | Cache all stable harness self-test families. | Seven process self-tests plus scaffold/changelog/README/schedule/statement/frame/cache families restore independently and fail closed on corruption or input drift. |
| LAT-03 | DONE | Cache whole-catalog behavior and milestone-surface validation. | Aggregate behavior/source descriptors restore both catalog stages; explicit source sets invalidate relevant changes. |
| LAT-04 | DONE | Cache source policy by tracked inventory and content. | The policy fingerprint covers every maintained source scope, configuration, attributes, and tracked policy input. |
| LAT-05 | DONE | Enforce and publish gate latency SLOs. | Versioned cold/hot samples are 52,000/4,718 ms; current measured runs passed at 48,584/2,625 ms against 90,000/10,000 ms SLOs. |

## Migration completion and harness consolidation

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| MIG-01 | DONE | Complete the second data-driven coordinator wave. | 386 simple and 35 composite declarative cycles leave 83 exceptional coordinators; both migrations preserve content-addressed evidence chains. |
| MIG-02 | DONE | Classify every remaining raw fixed-tick window. | The canonical gate validates 226 sources across 216 milestones and reports zero raw fixed-wait debt. |
| MIG-03 | DONE | Publish and aggregate await telemetry. | Every executed smoke emits an aggregate `WORLDLINE_AWAIT_TELEMETRY`; schema-2 history records waits, polls, failures, observed ticks, and rejects high-poll regressions. |
| MIG-04 | DONE | Persist retry dependence and alert reviewers. | Schema-2 nightly history records retry attempts, retries, failures, and policy calls; a previously clean smoke becoming retry-dependent fails review policy. |
| MIG-05 | DONE | Adopt generated narrative schema. | All 36 qualification-v1 milestones use generated combined narratives, and new scaffolds default to the same schema. |
| MIG-06 | DONE | Add a schema for every behavior map. | All 526 maps validate explicit boundary, bounded non-claims, and frozen-trace fields. |
| MIG-07 | DONE | Version and normalize `smoke.properties`. | All 525 descriptors use schema 1, declare their era and runner, and validate behavior plus TestKit identity. |
| MIG-08 | DONE | Burn down packed-line stock. | Mechanical, attested formatting reduces smoke and coordinator packed-line debt to zero without runtime requalification. |
| MIG-09 | QUEUED | Consolidate repeated B173, place/persist, and Aero parsing families. | Fingerprinted shared helpers replace the audited clones without changing behavior. |
| MIG-10 | QUEUED | Retire completed compatibility surfaces. | `Verify.java`, `SmokeLegacyImport`, and finalize-only migrators have dated removal trains and are deleted once their compatibility window closes. |

## Fingerprints and shared caches

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| CACHE-01 | QUEUED | Separate behavioral identity from execution-lane identity and promote portable pins safely. | Headless Windows/Linux differential evidence defines portable lanes; GUI/platform-sensitive proofs remain lane-bound. |
| CACHE-02 | QUEUED | Publish the first real smoke schedule aggregate. | Nightly observations populate versioned duration/failure/retry history; `schedule.properties` no longer falls back to an empty schema. |
| CACHE-03 | QUEUED | Add unified bounded GC for all shared caches. | Modules, tests, runners, receipts, and observations share age/usage/size policy and doctor reporting. |
| CACHE-04 | QUEUED | Normalize portable text as Unicode NFC. | Cross-platform fingerprint tests cover canonically equivalent Unicode plus CRLF/LF paths. |
| CACHE-05 | QUEUED | Decompose `RepositoryVerify` before further growth. | Cohesive verification stages live in bounded files and the original behavior remains fully covered. |

## Product, mappings, coverage, and external TestKit

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| COV-01 | QUEUED | Qualify whole-game mappings in batches through SEM-M11, M12, and M13. | Cross-corroborated namespace batches prioritize smoke-touched symbols and reach 25%, 50%, then 100% qualified symbols. |
| COV-02 | QUEUED | Rebalance public Atlas behavior families and resolve orphans. | The 109-placement concentration is split by function; all three retracted-orphan tokens are removed or requalified. |
| COV-03 | QUEUED | Add save/chunk-format coverage. | A deterministic save-format set establishes replay-safe chunk evidence. |
| COV-04 | QUEUED | Add worldgen coverage. | Biomes, caves, and ore-vein generation receive official-oracle sets. |
| COV-05 | QUEUED | Expand entity pathfinding coverage. | More than the single existing boundary covers deterministic pathfinding families. |
| COV-06 | QUEUED | Build a lighting-engine matrix. | A matrix closes the current partial lighting cases across generation and updates. |
| COV-07 | QUEUED | Expand weather coverage. | Deterministic weather transitions and persistence exceed the four partial cases. |
| COV-08 | QUEUED | Expand multiplayer edge-case coverage. | Connection, ordering, disconnect, and persistence edge sets close the identified gaps. |
| EXT-01 | QUEUED | Release TestKit 0.3.0 with current public behavior APIs. | Docs/examples use Gate, `WorldlineBehavior`, evidence pinning, an end-to-end vanilla expectation, and `worldline behaviors list`; the artifact is published only with later authorization. |
| EXT-02 | QUEUED | Split the three adapters currently at the file ceiling. | Dedicated-server, wire-client, and capture-mixin capabilities are decomposed below the limit before new features land. |
| EXT-03 | QUEUED | Connect GUI authoring to runtime structure. | A workbench screen proves `spec.matchesStructure(runtime.ui().nodes())` against the official oracle. |
| EXT-04 | QUEUED | Reconcile optimization constitution and practice. | Existing performance work receives stable optimization records/sites, or a reviewed constitutional amendment replaces the unused rule. |
| EXT-05 | QUEUED | Validate StationAPI as a second real driver. | A minimal StationAPI boot milestone exercises driver discovery, isolation, and TestKit behavior. |

## Documentation and program governance

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| DOC-01 | QUEUED | Finish repository documentation organization. | The 1,055 root documents migrate into populated feature/milestone/performance/project trees with a generated index, or the empty skeleton is removed. |
| DOC-02 | QUEUED | Rewrite architecture verification documentation. | Architecture covers Gate, Runtime Fabric, receipts, pools, caches, and current stage flow derived from verification evidence. |
| DOC-03 | QUEUED | Generate semantic and roadmap counts/status. | Semantic role totals and historical milestone prose derive from authoritative code/manifests and cannot drift manually. |
| DOC-04 | ACTIVE | Maintain this Fable 2 ledger and its exit gate. | Every objective-file requirement maps to one row, and each status change cites authoritative evidence. |

## Program exit gate

The Fable 2 program may be reported complete only when every row above is
`DONE`, or a genuinely unavoidable external row is `EXTERNAL` with a passing
doctor and exact operator action. The exact clean train SHA must pass the
canonical Gate and orchestrator gate. Any behavioral input without verified
evidence bytes executes again; no fingerprint or editable lock row alone can
manufacture a promotable PASS.
