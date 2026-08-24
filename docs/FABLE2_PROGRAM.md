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
| MIG-01 | DONE | Complete the second data-driven coordinator wave. | 386 simple and 35 composite declarative cycles leave 84 reviewed exceptional coordinators after the external-build M620 cycle; migrations preserve content-addressed evidence chains. |
| MIG-02 | DONE | Classify every remaining raw fixed-tick window. | The canonical gate validates 226 sources across 216 milestones and reports zero raw fixed-wait debt. |
| MIG-03 | DONE | Publish and aggregate await telemetry. | Every executed smoke emits an aggregate `WORLDLINE_AWAIT_TELEMETRY`; schema-2 history records waits, polls, failures, observed ticks, and rejects high-poll regressions. |
| MIG-04 | DONE | Persist retry dependence and alert reviewers. | Schema-2 nightly history records retry attempts, retries, failures, and policy calls; a previously clean smoke becoming retry-dependent fails review policy. |
| MIG-05 | DONE | Adopt generated narrative schema. | All 36 qualification-v1 milestones use generated combined narratives, and new scaffolds default to the same schema. |
| MIG-06 | DONE | Add a schema for every behavior map. | All 527 maps, including the aggregate map, validate explicit boundary, bounded non-claims, and frozen-trace fields. |
| MIG-07 | DONE | Version and normalize `smoke.properties`. | All 526 descriptors use schema 1, declare their era and runner, and validate behavior plus TestKit identity. |
| MIG-08 | DONE | Burn down packed-line stock. | Mechanical, attested formatting reduces smoke and coordinator packed-line debt to zero without runtime requalification. |
| MIG-09 | DONE | Consolidate repeated B173, place/persist, and Aero parsing families. | One fingerprinted B173 fixture helper replaces exact clones in 354 sources, the two Aero coordinators share a strict parser, 354 pins are transported by reviewed source hashes, and the canonical Gate validates all 525 evidence envelopes. |
| MIG-10 | DONE | Retire completed compatibility surfaces. | The dated 2026-08-23 removal train deletes `Verify.java`, the EOF/fixed-wait finalize-only migrators and their Gate routes; `SmokeLegacyImport` was already removed, while immutable locks and permanent drift checks remain. |

## Fingerprints and shared caches

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| CACHE-01 | ACTIVE | Separate behavioral identity from execution-lane identity and promote portable pins safely. | The exclusive differential recorder and fail-closed seal are implemented; one clean Windows/Linux official headless comparison remains before the identity switch. |
| CACHE-02 | DONE | Publish the first real smoke schedule aggregate. | Seven retained runtime histories populate duration/failure fields, their source hashes form a non-empty ratchet, schema-1 local observations upgrade structurally, and the Gate rejects unknown or regressed rows. |
| CACHE-03 | DONE | Add unified bounded GC for all shared caches. | Modules, tests, runners, receipts, observations and verification caches share one 20 GiB/30-day policy, digest usage markers, locks, doctor/GC aliases and self-tests; the doctor reports 3,230 non-module entries across eight families. |
| CACHE-04 | DONE | Normalize portable text as Unicode NFC. | Strict UTF-8 text normalizes LF and NFC while binary stays byte-exact; cross-platform tests cover CRLF and decomposed/composed Unicode, and the versioned migration transports all 525 proofs. |
| CACHE-05 | DONE | Decompose `RepositoryVerify` before further growth. | Module configuration and source policy now live in bounded cohesive classes, `RepositoryVerify` fell below its former ceiling, and the canonical Gate covers the unchanged stage flow. |

## Product, mappings, coverage, and external TestKit

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| COV-01 | DONE | Qualify whole-game mappings in batches through SEM-M11, M12, and M13. | The clean runtime profile reconstructed the official-input-bound 25%, 50%, and 100% policies over 6,475 maintained identities: SEM-M11 `74eda1fa0dc3fa38ef67ac8cda9039d0c87ccd4d41bab7033886a96fbaf4ef32`, SEM-M12 `95effb9dda816c2dfe4e48cccebbe26e44a2aed0d4235f4e1f1832278b1800f0`, and SEM-M13 `c8e21ca4a15ab7f4ab211b5ab6f66e191281910f896951b67b310ba15907f91e`. |
| COV-02 | ACTIVE | Rebalance public Atlas behavior families and resolve orphans. | The former 109-placement concentration is now seven function-specific contracts with a maximum bucket of 34 and 520 portable proofs carried. Grok M615 and M617 cover two overlapping boundaries under different token names, while M614 is not yet committed; coordinator reconciliation must choose the canonical identities and bind their exact evidence before the orphan set is resolved. |
| COV-03 | DONE | Add save/chunk-format coverage. | M621's clean official-runtime receipt at `e1d4c9dab9d2aa4007ceb8c07bde87bfd2924ad0` validates nine McRegion location, timestamp, sector, zlib, and NBT-root frames, then proves stable geology across a clean save/restart. Its frozen signature is `269269a06fb4fbd3833715a554c0830bbc3bd6c70e6e2db13348ac5bde2bad10`. |
| COV-04 | DONE | Add worldgen coverage. | The same bounded M621 3x3 official-oracle set requires multiple surface/elevation families, subsurface cave air, nonzero ore blocks, and at least three connected ore components while explicitly excluding arbitrary seeds and structure-generation claims. |
| COV-05 | DONE | Expand entity pathfinding coverage. | M622 adds a direct official-oracle pathfinder matrix over open terrain, a forced wall-gap detour, and a sealed-target fallback. Its four-process trace is frozen at `7d60a218116c3281ab77011768f14b4237d0b92b81f3e0d99cdb6fabb085029a` and the clean milestone receipt is bound to `17d42785ef39f4744aa1e19f5a4b25e355363856`. |
| COV-06 | DONE | Build a lighting-engine matrix. | M623 compares mapped and official generation over open, roofed, and aperture terrain, then freezes queued replacement/attenuation for emission levels 15, 13, 9, and 7 plus block- and sky-light recovery. Its signature is `b1bf8088d22536e61795483c655bc8e1d82eed18d0e6298e0859aa40525435fb`, qualified at `4ed43e62dee69ce69f92d77d3b3b174bd44d4011`. |
| COV-07 | DONE | Expand weather coverage. | M624 freezes five mapped/official cases covering both rain toggles, both thunder toggles, a simultaneous storm transition, state-specific countdown reseeding, exact strength ramps, and NBT persistence. Its signature is `b96fb0662d743ed48b06c1346b4f18210823c33353aeed64b310a19db86892ad`, qualified at `a50feb66808f9e4c151928cfd6a5f664cc766b75`. |
| COV-08 | DONE | Expand multiplayer edge-case coverage. | M625 proves two connection orders, isolated peer disconnect, same-user reconnect with a fresh entity ID, and pose persistence across disconnect plus a clean server restart. Its signature is `d63e6e3d624950298e04542285ce3e9639d5d15be3b3d0866b35a9549f98e878`, qualified at `8bae6bf2ceb3b7288c48668c9c8e8b75f735d56f`. |
| EXT-01 | DONE | Release TestKit 0.3.0 with current public behavior APIs. | Versioned code/docs, the release-ready manifest, `worldline behaviors list`, and an end-to-end vanilla expectation are implemented; exact clean-commit milestone receipts for `m7-mod-loading`, `m8-mod-version-diff`, `m9-scenario-minimization`, and `testkit-cycle` are validated and carried by `qualification.lock`. Publication remains tag-authorized only. |
| EXT-02 | DONE | Split the three adapters currently at the file ceiling. | Dedicated-server process control, protocol-14 login, and Aero capture settings/scene work are seven cohesive sources; the former ceiling files are now 100, 139, and 99 code lines, and a source-attested migration carries 438 affected fingerprints across all 525 evidence envelopes. |
| EXT-03 | DONE | Connect GUI authoring to runtime structure. | `GameUiSpec.workbench()` and the b1.7.3 driver expose the exact 47-node result/matrix/player tree; the extended four-process `gui-tree` differential has an exact clean-commit official-runtime receipt validated and carried by `qualification.lock`. |
| EXT-04 | DONE | Reconcile optimization constitution and practice. | Five owned build, Gate, and Runtime Fabric optimizations now have stable records and ten symbol-tracked sites; the catalog resolves every declared type/method uniquely and rejects absent members, while external Aero algorithms remain correctly project-owned. |
| EXT-05 | DONE | Validate StationAPI as a second real driver. | M620 implements SPI discovery plus a process-isolated provider that gates a real StationAPI client tick and runs two fresh TestKit sessions; its exact clean-commit official-runtime receipt is validated and carried by `qualification.lock`. |

## Documentation and program governance

| ID | Status | Deliverable | Completion evidence |
| --- | --- | --- | --- |
| DOC-01 | DONE | Finish repository documentation organization. | `DocumentationCatalog` classifies every stable root document into project, feature, milestone, or performance sections in a generated index; stable paths avoid invalidating milestone receipts, and no empty directory skeleton is versioned. |
| DOC-02 | DONE | Rewrite architecture verification documentation. | `VERIFICATION_ARCHITECTURE.md` derives the canonical profile/stage flow from Gate and RepositoryVerify, separates behavioral identity from lanes, and documents Runtime Fabric leases, host/container pools, evidence envelopes, immutable caches, and orchestrator authority. |
| DOC-03 | DONE | Generate semantic and roadmap counts/status. | Generated status derives semantic totals from `SemanticRoles`, program counts from this ledger, and milestone qualification from current fingerprints plus `qualification.lock`; Gate rejects drift. |
| DOC-04 | ACTIVE | Maintain this Fable 2 ledger and its exit gate. | Every objective-file requirement maps to one row, and each status change cites authoritative evidence. |

## Program exit gate

The Fable 2 program may be reported complete only when every row above is
`DONE`, or a genuinely unavoidable external row is `EXTERNAL` with a passing
doctor and exact operator action. The exact clean train SHA must pass the
canonical Gate and orchestrator gate. Any behavioral input without verified
evidence bytes executes again; no fingerprint or editable lock row alone can
manufacture a promotable PASS.
