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
| PIPE-01 | ACTIVE | Make bounded Linux-container and Windows-job runtime pools a canonical cold-sweep backend. | Host workers now enter through commit-bound `Gate --milestone`; container parity and CI routing remain. |
| PIPE-02 | DONE | Pin every accepted smoke in the portable qualification lock. | `smokes/qualification.lock` has 525 current v2 fingerprints; the final qualification restored 525 pins and executed zero smokes. |
| PIPE-03 | DONE | Cache smoke-runner compilation per runner or coherent group. | Each coordinator has an immutable input-addressed entry; unchanged coordinators are neither compiled nor copied into the transient build tree. |
| PIPE-04 | DONE | Add content-addressed PASS receipts for unit-suite execution. | Suite, compiled-test digest, execution-model sources, Java runtime, assertions, and hashed evidence bind each immutable local PASS proof; absent or altered evidence executes again. |
| PIPE-05 | DONE | Remove whole-tree publication copies on module-cache hits. | Module outputs are symbolic links on POSIX and directory junctions on Windows, verified to resolve to immutable cache entries. |
| PIPE-06 | DONE | Size module build workers from the machine by default. | Default is half the available processors, clamped to a safe range; the environment override remains authoritative. |
| PIPE-07 | DONE | Validate only affected milestone surfaces in candidate mode. | `CandidateCheck` validates the requested milestone contract and affected module tests; `RepositoryVerify` retains full-catalog validation for release profiles. |
| PIPE-08 | DONE | Cache nested harness self-tests by harness digest. | Harness sources, Java, OS, and control filesystem bind a hashed PASS proof; any change reruns the lock test. |
| PIPE-09 | DONE | Restrict source-quality traversal to tracked files. | `SourceQualityCheck` consumes the fail-closed tracked-file inventory. |
| PIPE-10 | ACTIVE | Schedule cold smokes using reviewed duration/failure history. | `Gate --smoke-plan` orders missing proofs by cached duration; failure-frequency ranking remains. |

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
| FLAKE-02 | ACTIVE | Centralize EOF retry and publish retry telemetry. | Cycle retry policy lives in one support class and every retry is counted in evidence. |
| FLAKE-03 | QUEUED | Consolidate the 17 copied `WorldlinePagedAeroMixin` sources. | One shared fingerprinted source serves all consumers. |
| FLAKE-04 | QUEUED | Replace line-pressure statement packing with a statement/debt ratchet. | Smoke policy measures executable statements/helpers and the long-line debt decreases monotonically. |

## Marginal cost per milestone

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| SCALE-01 | QUEUED | Add `Gate --new-milestone ID` deterministic scaffolding. | Generated descriptors, cycle, smoke, docs, map, and tests pass candidate validation without hand copying. |
| SCALE-02 | QUEUED | Replace clone-like cycle launchers with a data-driven runner. | Ordinary milestones use one runner; exceptional scenarios remain explicit. |
| SCALE-03 | QUEUED | Generate repeated claim/cycle/map narration from one canonical descriptor. | No claim/SHA fact is manually maintained in three places. |
| SCALE-04 | QUEUED | Partition or generate the large changelog by release series. | Integration no longer edits one 252 KB coordinator hotspot. |
| SCALE-05 | QUEUED | Generate the README status table during integration. | Manual status-table edits fail the gate. |
| SCALE-06 | QUEUED | Remove `qualification.lock` merge contention. | Ordered-union merge support or per-smoke pin files has deterministic tests. |

## Continuous integration

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| CI-01 | QUEUED | Cache or checksum-pin the `tokei` executable. | Verify jobs do not compile it on every run. |
| CI-02 | QUEUED | Add PR concurrency cancellation. | Superseded pushes stop their old verify jobs. |
| CI-03 | QUEUED | Persist content-addressed module/test caches in CI. | Cache keys include the required platform/toolchain identity. |
| CI-04 | QUEUED | Publish `verify.json` timings in the job summary. | Regressions are visible without downloading artifacts. |
| CI-05 | QUEUED | Derive TestKit artifact checksums from the release tag. | Publishing cannot retain a stale hard-coded release checksum. |
| CI-06 | QUEUED | Run a nightly pooled sweep and open a reviewed pin-update PR. | The workflow never pushes unreviewed proofs directly to `main`. |

## Agent-swarm process

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| SWARM-01 | QUEUED | Require scoped CSM memory for every flake/fixture correction. | Done checks reject a correction lacking a reusable `nya remember` scar. |
| SWARM-02 | QUEUED | Require NWC deferments for non-integrated experiments. | Every prototype has a verifiable cue, owner, and expiry. |
| SWARM-03 | QUEUED | Version an auditable handoff registry. | Worktree path, clean SHA, base, receipt, and disposition survive task history. |
| SWARM-04 | QUEUED | Generate a swarm dashboard. | One HTML report joins worktrees, triage, pins, handoffs, and timings. |
| SWARM-05 | ACTIVE | Enforce reconciliation history policy. | Main receives one reviewable commit per logical milestone/batch, with iterative repair commits squashed before promotion. |
| SWARM-06 | QUEUED | Enforce one branch naming scheme. | New work uses the documented `codex/<kind>-<id>-<slug>` grammar; invalid train refs fail closed. |

## Product and test quality

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| QUAL-01 | QUEUED | Ensure every `Remote*Test` is registered or explicitly excluded. | Gate discovery compares tracked classes with the suite registry. |
| QUAL-02 | QUEUED | Reject orphaned `__snapshots__/*.wlsnap` files. | Every tracked snapshot resolves to an owning test. |
| QUAL-03 | QUEUED | Give differential fuzzing and mutation nightly time budgets. | Fast deterministic unit checks remain local; bounded exploratory jobs publish artifacts nightly. |
| QUAL-04 | ACTIVE | Connect frame census to scene-relative performance budgets. | Frame census/breakdown exists; machine-relative Aero scene budgets and gate enforcement remain. |
| QUAL-05 | QUEUED | Refocus source policy on smoke maintainability. | Product ceilings remain fail-closed while the smoke statement/helper ratchet replaces counterproductive packing. |

## Windows and Git hygiene

| ID | Status | Deliverable | Evidence or completion condition |
| --- | --- | --- | --- |
| WIN-01 | QUEUED | Add a safe repository maintenance setup/doctor. | It configures maintenance, commit-graph, fsmonitor when supported, and untracked cache per clone. |
| WIN-02 | EXTERNAL | Measure and optionally configure Defender exclusions. | Repository tooling reports measured impact and prints an explicit administrator command; it never weakens security silently. |
| WIN-03 | QUEUED | Enable and verify long-path support. | Per-repository `core.longpaths` is set; OS capability is diagnosed with an exact administrator action if absent. |
| WIN-04 | QUEUED | Prune stale private build/output trees during lifecycle archival. | Cleanup is scoped under validated archived worktrees and reports recoverability. |

## Program exit gate

The Fable program may be reported complete only when every row is `DONE`, or an
`EXTERNAL` row has both a passing fail-closed doctor and its unavoidable operator
action documented. Static checks must pass on the exact clean candidate. Runtime
proofs are restored from current fingerprints whenever possible; only changed or
new behavioral inputs execute again.
