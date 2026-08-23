# Fable Total-Program Ledger

This ledger is the release contract for the Fable efficiency report accepted on
2026-08-23. An item is complete only when its implementation, automated check,
documentation, and reproducible evidence are all present on `main`. Items that
need an external capability remain open with a fail-closed doctor and an exact
operator action; they are never silently waived.

Status values are `DONE`, `ACTIVE`, `QUEUED`, and `EXTERNAL`.

## Integration and repository hygiene

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| INT-01 | DONE | Reconcile every unique pre-train line before promotion. | The semantic audit, Squid land-death, frame census, anti-slop, GUI memory admission, mappings, Atlas, and runtime-fabric work are reachable from `main`. |
| INT-02 | DONE | Extract the four non-equivalent `pending-integration` patches and archive the obsolete line. | Reconciliation completed before the 2026-08-23 cleanup; no live `pending-integration` ref remains. |
| INT-03 | DONE | Bundle and archive contained branches and remove dead worktrees. | Recovery bundles live outside the repository in `worldline-archives`; the repository has one worktree and only `main` after each completed train. |
| INT-04 | DONE | Add repeatable branch triage with JSON output. | `WorktreeLifecycle triage` writes `.worldline/reports/branches.json`; `IntegrationTrain` invokes it after a train. |
| INT-05 | DONE | Mark superseded M513/M514/M515/M518/M521/M524 attempts as abandoned. | Their refs/worktrees were archived during cleanup and are not resumable live branches. |
| INT-06 | ACTIVE | Use small, frequent, exact-SHA integration trains. | Each program batch must pass Gate and the orchestrator gate, then restore the one-branch/one-worktree baseline. |

## Pipeline and official-runtime throughput

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| PIPE-01 | DONE | Make bounded Linux-container and Windows-job runtime pools a canonical cold-sweep backend. | Host and Docker workers enter through commit/image-bound `Gate --milestone`; cached Gate self-tests cover all backends and nightly CI routes headless/GUI lanes explicitly. |
| PIPE-02 | DONE | Pin every accepted smoke in the portable qualification lock. | `smokes/qualification.lock` has 525 current v4 fingerprints; qualification restores all pins and executes zero unchanged smokes. Runtime observations use a separate execution-only fingerprint, so a changed budget or expected signal revalidates sealed evidence without rerunning Minecraft. |
| PIPE-03 | DONE | Cache smoke-runner compilation per runner or coherent group. | Each coordinator has an immutable input-addressed entry; unchanged coordinators are neither compiled nor copied into the transient build tree. |
| PIPE-04 | DONE | Add content-addressed PASS receipts for unit-suite execution. | Suite, compiled-test digest, execution-model sources, Java runtime, assertions, and hashed evidence bind each immutable local PASS proof; absent or altered evidence executes again. |
| PIPE-05 | DONE | Remove whole-tree publication copies on module-cache hits. | Module outputs are symbolic links on POSIX and directory junctions on Windows, verified to resolve to immutable cache entries. |
| PIPE-06 | DONE | Size module build workers from the machine by default. | Default is half the available processors, clamped to a safe range; the environment override remains authoritative. |
| PIPE-07 | DONE | Validate only affected milestone surfaces in candidate mode. | `CandidateCheck` validates the requested milestone contract and affected module tests; `RepositoryVerify` retains full-catalog validation for release profiles. |
| PIPE-08 | DONE | Cache nested harness self-tests by harness digest. | Harness sources, Java, OS, and control filesystem bind a hashed PASS proof; any change reruns the lock test. |
| PIPE-09 | DONE | Restrict source-quality traversal to tracked files. | `SourceQualityCheck` consumes the fail-closed tracked-file inventory. |
| PIPE-10 | DONE | Schedule cold smokes using reviewed duration/failure history. | Executions record idempotent attempt/failure/duration observations; nightly review versions the aggregate, and cold plans order failure rate first, then average duration and stable ID. |

## Harness robustness

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| HAR-01 | DONE | Bound every repository verification subprocess. | `ProcessCapture` enforces a configurable timeout and includes the captured tail. |
| HAR-02 | DONE | Kill descendant trees consistently. | Gate capture, smoke-runner build, test build, and integration-train teardown use recursive reverse-order termination. |
| HAR-03 | DONE | Normalize paths in cross-platform cache digests. | Gate harness and smoke-runner digests use `/`, matching module/test behavior. |
| HAR-04 | DONE | Replace regex JSON parsing in orchestrator/push checks. | `MiniJson` strictly parses plans and current receipts; tests cover escapes, nested values, duplicate keys, malformed escapes, and trailing data. |
| HAR-05 | DONE | Preserve exception stack traces in machine reports. | `verify.json` includes escaped `stack_trace`. |
| HAR-06 | DONE | Attach suite log tails to timeout/failure reports. | `TestBuild` reports the bounded captured tail. |
| HAR-07 | DONE | Replace lock busy-polling with a fair FIFO ticket queue. | Monotonic FIFO tickets order official-runtime and verify-slot acquisition; dead-process tickets are pruned and contention order is self-tested. |
| HAR-08 | DONE | Provide a platform-neutral Java pre-push entry point. | Shell and Windows launchers bootstrap `PrePushCheck`; Java owns ref classification, current-harness verification, Gate selection, and exact-SHA authorization. |

## Smoke determinism and flakiness debt

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| FLAKE-01 | ACTIVE | Provide shared `awaitSlot`, `awaitBlock`, and `awaitEntity` polling contracts and migrate fixed-wait assertions. | The shared support records attempts/duration; the fixed-wait ratchet reaches zero without invalidating unrelated pins. |
| FLAKE-02 | DONE | Centralize EOF retry and publish retry telemetry. | `SmokeRetry` owns bounded decisions, backoff, counters, and shutdown telemetry for the generic runner and all 33 exceptional coordinators. Both legacy EOF ratchets are zero; the migration lock binds prior/current sources, fingerprints and evidence, and M258 passed a fresh official-runtime qualification. |
| FLAKE-03 | DONE | Consolidate the 17 copied `WorldlinePagedAeroMixin` sources. | Two fingerprinted shared variants preserve the base/direct distinction; the gate rejects consumer copies and routing drift. |
| FLAKE-04 | DONE | Replace line-pressure statement packing with a statement/debt ratchet. | A literal-aware statement lexer enforces 300/150 ceilings, path-specific legacy debt and non-growing packed-line density. |

## Marginal cost per milestone

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| SCALE-01 | DONE | Add `Gate --new-milestone ID` deterministic scaffolding. | The overwrite-safe generator creates a byte-deterministic descriptor, draft cycle, claim/cycle docs, and map; self-tests compile the runner and validate the complete candidate topology while runtime qualification remains fail-closed. |
| SCALE-02 | DONE | Replace clone-like cycle launchers with a data-driven runner. | 309 mechanically homogeneous official-server milestones use `DataDrivenCycle`; 196 exceptional coordinators remain explicit. The migration lock binds every deleted source hash and prior proof to its validated declarative plan and shared sources; M297 also passed a fresh representative official-runtime qualification. |
| SCALE-03 | DONE | Generate repeated claim/cycle/map narration from one canonical descriptor. | Narrative schema 1 byte-validates one combined claim/cycle document rendered from `smoke.properties`; the semantic map remains independently authored. |
| SCALE-04 | DONE | Partition or generate the large changelog by release series. | `CHANGELOG.md` is a bounded index; release sections live in validated coordinator-owned series files, with current-version, uniqueness, routing, and content-preservation checks. |
| SCALE-05 | DONE | Generate the README status table during integration. | `ReadmeStatus update` derives the badge and bounded table from release, coverage, catalog, and portable-pin inputs; the release gate rejects manual edits or stale counts. |
| SCALE-06 | DONE | Remove `qualification.lock` merge contention. | A configured, deterministically tested three-way driver unions disjoint sorted pins and rejects conflicting edits to the same pin. |

## Continuous integration

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| CI-01 | DONE | Cache or checksum-pin the `tokei` executable. | The shared setup action caches pinned `tokei` 14.0.0 binaries by runner OS and installs only on a cache miss. |
| CI-02 | DONE | Add PR concurrency cancellation. | Verify concurrency cancels superseded PR runs while retaining push runs. |
| CI-03 | DONE | Persist content-addressed module/test caches in CI. | Hosted verification restores the immutable Gate cache by runner OS and Java 21 identity. |
| CI-04 | DONE | Publish `verify.json` timings in the job summary. | `VerifySummary` renders stage timings in every Gate workflow, including a safe missing-report result. |
| CI-05 | DONE | Derive TestKit artifact checksums from the release tag. | `TestKitReleaseCheck` binds the tag, package version, exact artifact set, and generated SHA-256 values. |
| CI-06 | DONE | Run a nightly pooled sweep and open a reviewed pin-update PR. | The scheduled private workflow plans missing proofs, routes them through Runtime Fabric, pins successes, and opens a lockfile-only PR. |

## Agent-swarm process

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| SWARM-01 | DONE | Require scoped CSM memory for every flake/fixture correction. | Reliability-labelled runtime corrections are rejected by the train unless their diff includes a scoped NYA scar. |
| SWARM-02 | DONE | Require NWC deferments for non-integrated experiments. | Worktree audit rejects every `codex/experiment-*` branch without a branch-bound tracked NWC deferment. |
| SWARM-03 | DONE | Version an auditable handoff registry. | `SwarmHandoff` records and validates branch, worktree, clean SHA, base, receipt path/hash, disposition, and time. |
| SWARM-04 | DONE | Generate a swarm dashboard. | `SwarmDashboard` joins worktree/branch reports, portable pins, versioned handoffs, and latest Gate timings in one escaped HTML report. |
| SWARM-05 | DONE | Enforce reconciliation history policy. | Integration rejects candidates with anything other than one reviewed logical commit over the declared base. |
| SWARM-06 | DONE | Enforce one branch naming scheme. | Trains enforce `codex/<kind>-<id>-<slug>`, restrict reconcile to `train`, and reject `experiment` integration. |

## Product and test quality

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| QUAL-01 | DONE | Ensure every `Remote*Test` is registered or explicitly excluded. | The gate discovers tracked API `Remote*Test` classes, requires one `run()` entry point, and one `DomainApiTest` registration. |
| QUAL-02 | DONE | Reject orphaned `__snapshots__/*.wlsnap` files. | Every tracked snapshot requires a tracked owner sidecar bound to an existing test source and literal snapshot declaration. |
| QUAL-03 | DONE | Give differential fuzzing and mutation nightly time budgets. | Fast deterministic checks remain local; the scheduled/manual quality campaign splits a hard timeout between seeded differential and mutation-manifest exploration and publishes logs plus JSON evidence. |
| QUAL-04 | DONE | Connect frame census to scene-relative performance budgets. | M74's two same-host absent/present pairs gate median/p95/p99/max against reviewed ratio-plus-slack limits before a receipt can be published. |
| QUAL-05 | DONE | Refocus source policy on smoke maintainability. | Product/adapter ceilings remain fail-closed; smoke statement debt and fingerprinted shared-helper families replace counterproductive packing. |

## Windows and Git hygiene

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| WIN-01 | DONE | Add a safe repository maintenance setup/doctor. | `RepositoryMaintenance setup` configures maintenance, commit-graph, fsmonitor when supported, and untracked cache per clone. |
| WIN-02 | EXTERNAL | Measure and optionally configure Defender exclusions. | The passing doctor reports a current-path probe and exact optional administrator command; it never weakens security silently. |
| WIN-03 | DONE | Enable and verify long-path support. | Setup enables `core.longpaths`; the doctor verifies the OS registry capability and prints the exact administrator action if absent. |
| WIN-04 | DONE | Prune stale private build/output trees during lifecycle archival. | Archive cleanup is confined to `.worldline`, `tmp`, and `output` under the validated worktree and reports unrecoverable bytes. |

## Program exit gate

The Fable program may be reported complete only when every row is `DONE`, or an
`EXTERNAL` row has both a passing fail-closed doctor and its unavoidable operator
action documented. Static checks must pass on the exact clean candidate. Runtime
proofs are restored from current fingerprints whenever possible; only changed or
new behavioral inputs execute again.
