# Engineering workflow

Worldline supports concurrent milestone work without sharing mutable build directories,
official-runtime processes, or release indexes. This document is normative for repository
coordination; `AGENTS.md` remains the concise entry point.

## One milestone, one worktree

Each milestone worker owns one clean worktree, one local branch, its `smokes/<id>/` directory,
and the smallest necessary product or adapter files. A worker commits and qualifies locally,
then hands the exact worktree path and SHA to the orchestrator. Workers do not push milestone
branches and do not merge `main`. Workers must not edit the coordinator files below:

- `CHANGELOG.md`;
- `README.md`;
- `docs/ROADMAP.md`;
- `docs/ARCHITECTURE.md`;
- `release/worldline.properties`;
- `modules/api/src/main/java/worldline/api/WorldlineVersion.java`.

The integration coordinator updates those files once for an accepted train. Frozen behavior
signatures live in each milestone's `smoke.properties`; the release manifest contains only
global identity, canonical inputs, core signatures, and the currently promoted milestone.

## Verification tiers

| Command | Purpose | Official runtime |
| --- | --- | --- |
| `java tools/harness/Gate.java --candidate <id>` | Compile the runner/scenario and run affected module tests | Never |
| `java tools/harness/Gate.java --milestone <id>` | Fully qualify one clean, committed milestone | Exclusive cycle only |
| `java tools/harness/Gate.java --orchestrator` | Bind an integrated train and global gate to one pushable SHA | Never |
| `java tools/harness/Gate.java` | Full repository compile and unit gate | Never |
| `java tools/harness/Gate.java --runtime` | Repository gate plus required local artifact validation | Reads identities only |
| `java tools/harness/Gate.java --smoke` | Qualify every discovered milestone | Exclusive |

Do not invoke `tools/smoke/*.java` directly. The gate owns the machine runtime lease, process
timeout, descendant cleanup, and `.worldline/smoke-logs/` output. `Verify.java` remains a
compatibility launcher and cannot bypass the gate. `--smoke-id` remains an alias for
`--milestone` for old automation.

The milestone gate deliberately has two phases. Static compilation, contract checks, and
affected tests use the shared verification slots and may run across many worktrees. Only the
executable cycle queues for the official-runtime lock. This keeps the scarce runtime idle only
for real evidence execution, not for compilation.

## Milestone definition of done

A milestone may be pushed or offered to an integration train only after all of these are true:

1. its descriptor has a frozen signal and SHA-256, never `pending`;
2. `smokes/<id>/MAP.md` records the semantic boundary and evidence;
3. one milestone document and `docs/M<number>_CYCLE.md` describe the claim and full cycle;
4. any mapping hash is frozen in the descriptor;
5. a public behavior is registered under its semantic token and `atlas.scenario.<token>`;
6. the isolated runner completes through the official-runtime lease; and
7. `.worldline/reports/milestones/<id>.json` binds the result to the clean Git commit, tree,
   base revision, frozen signature, and runtime-log digest.

Passing these checks creates a handoff candidate, not push authority. The worker reports its
worktree path and commit SHA to the orchestrator and stops. The ignored local receipt remains
available for audit in that worktree.

For every new milestone, add the explicit qualification block below to `smoke.properties`.
Historical milestones retain their inferred contract, but Atlas-backed historical descriptors
also carry `behavior=<token>`.

```properties
behavior=example
qualification.schema=1
qualification.proof=official-cycle
qualification.docs=docs/M470_EXAMPLE.md
qualification.cycle=docs/M470_CYCLE.md
qualification.semantic-map=smokes/m470-example/MAP.md
qualification.atlas=atlas.scenario.example
qualification.testkit=behavior-evidence
```

Use `qualification.proof=tooling-cycle` when no official client/server JAR is involved. If the
milestone does not publish a behavior, use `qualification.atlas=not-applicable` plus a concrete
`qualification.atlas.reason`. This is an explicit reviewed non-claim, not permission to omit an
applicable Atlas entry. Also use `qualification.testkit=not-applicable` with a concrete
`qualification.testkit.reason`. Atlas-backed behavior instead requires `behavior-evidence`; the
gate verifies both Atlas resolution and the TestKit evidence-comparison implementation.

The gate shares immutable module/test caches between worktrees and publishes private outputs
under each worktree's `.worldline/`. The following environment variables are supported:

| Variable | Default | Meaning |
| --- | --- | --- |
| `WORLDLINE_CONTROL_DIR` | OS user runtime-data directory | Shared lock/cache directory |
| `WORLDLINE_RUNTIME_LOCK` | Auto-detected legacy swarm lock | Compatibility runtime lock |
| `WORLDLINE_VERIFY_SLOTS` | Half the CPUs, capped at 4 | Concurrent repository gates |
| `WORLDLINE_BUILD_WORKERS` | 2 | Module DAG compilation workers |
| `WORLDLINE_TEST_WORKERS` | 4 | Test compilation/execution workers |
| `WORLDLINE_TEST_TIMEOUT_SECONDS` | 180 | Per-test JVM timeout |
| `WORLDLINE_SMOKE_TIMEOUT_SECONDS` | Descriptor value or 900 | Per-smoke process timeout |
| `WORLDLINE_SMOKE_CACHE` | `on` | Set to `off` to bypass PASS-proof reuse |
| `WORLDLINE_GATE_WAIT_SECONDS` | 7200 | Lock acquisition timeout |

## Incremental smoke qualification

The complete smoke gate is resumable and content-addressed. After each scenario passes, the
gate atomically stores its log and PASS proof in the shared control-directory cache. An
interruption therefore loses only the scenario that was still running. On the next invocation,
the gate recomputes each scenario's behavior-input fingerprint and reuses only an intact proof
with the same fingerprint.

The tracked `smokes/qualification.lock` is the portable layer above that machine-local cache. It
records the fingerprint, PASS status, and evidence hash for each explicitly pinned milestone.
A fresh clone may reuse a matching reviewed pin without downloading the heavy log. The lockfile
is deliberately outside every milestone fingerprint, so committing updated pins does not create
a circular invalidation.

The fingerprint includes the milestone directory, its runner, shared runner support when used,
official-artifact descriptors, referenced adapters and toolchains, referenced product modules
with their transitive module dependencies, the Java runtime, and the operating-system
architecture. Documentation or release-index edits outside those inputs do not invalidate
runtime evidence. A new milestone has no proof and runs by itself; a shared adapter or product
change invalidates the scenarios that consume it. Malformed, missing, or altered proofs are
cache misses and fail closed to real execution.

`SmokeLegacyImport` exists only to migrate reports created before the receipt cache was
introduced. A passed report may migrate the complete suite. A failed report may migrate only the
strictly completed prefix before its identified failure. Both modes require a clean worktree,
fresh logs, and explicit completion rows in the report; failed, stale, or ambiguous individual
results are rejected.

Pin a completed clean suite explicitly, then review and commit the lockfile:

```text
java tools/harness/Gate.java --pin-smokes
git diff -- smokes/qualification.lock
```

A normal gate never modifies the tracked lock. Git review is the initial trust boundary; a
protected CI deployment may additionally require signed commits or an external attestation.
The lock format retains evidence hashes so that such an attestor can be added without changing
the fingerprint model. Local logs remain optional cache artifacts and are never committed.

After all discovered scenarios are either executed or reused, the gate writes per-scenario
attestations under `.worldline/reports/smokes/` and an aggregate
`.worldline/reports/smoke-suite.json`. The aggregate is bound to the exact Git HEAD and tree and
records executed/reused counts plus a root hash over every proof. Dirty worktrees may use the
cache for development, but their aggregate cannot authorize promotion. `Gate.java
--orchestrator` recomputes the fingerprints and aggregate and requires a clean receipt for its
exact commit. `WORLDLINE_SMOKE_CACHE=off` is the explicit full-rerun escape hatch.

## Integration train

After workers commit, individually qualify, and hand off their clean worktrees, the orchestrator
qualifies them again against the exact shared integration base:

```text
java tools/integration/IntegrationTrain.java --base <base-sha> \
  m470-example=refs/heads/codex/m470-example \
  m471-example=refs/heads/codex/m471-example
```

The train rejects stale ancestry, coordinator-owned changes, overlapping path ownership, and
pairwise merge conflicts. It locates every registered clean worktree and launches the complete
milestone gate with the same base SHA. Static phases run concurrently; official cycles serialize
inside the Gate. The resulting plan is written to `.worldline/reports/integration-plan.json`.
`--plan-only` performs conflict/ownership planning but is explicitly non-qualifying. Merge only a
passing qualified plan into a clean staging branch, then run the orchestrator gate once.

For a one-time historical reconciliation that already owns coordinated release
files, qualify exactly one consolidated candidate:

```text
java tools/integration/IntegrationTrain.java --base <base-sha> \
  --reconcile full-integration=<ref>
```

Reconciliation mode runs the complete smoke gate on that clean commit before
writing the qualified plan; it cannot combine multiple candidates.

After merging and reconciling the candidates in a clean committed staging worktree, create the
push authorization:

```text
java tools/harness/Gate.java --smoke
java tools/harness/Gate.java --orchestrator
WORLDLINE_ORCHESTRATOR_PUSH=1 git push <remote> <staging-or-main-ref>
```

The staging smoke invocation normally reuses the base proofs and the PASS proofs written by each
candidate's isolated milestone gate; it executes only inputs changed by reconciliation. Its job
is to aggregate those proofs and bind them to the newly created staging SHA.

The orchestrator gate requires a verified `integration-plan.json` and the aggregate smoke receipt
for its exact clean commit, proves that every candidate SHA and the declared base are ancestors
of the current commit, recomputes all smoke fingerprints, runs the complete repository gate, and
writes `.worldline/reports/orchestrator-push.json`. The pre-push hook checks the receipt,
integration-plan and smoke-suite digests, and exact local SHA. Setting the environment variable
without matching receipts cannot authorize a push. Any new commit invalidates the authorization,
but unchanged content-addressed PASS proofs make reattestation incremental.

## Worktree lifecycle

Audit registered worktrees without changing them:

```text
java tools/integration/WorktreeLifecycle.java audit --base <integrated-ref>
```

The report at `.worldline/reports/worktrees.json` records existence, dirty state, ancestry,
and archive eligibility. `prune` is dry-run only. Archival is deliberately explicit:

```text
java tools/integration/WorktreeLifecycle.java archive \
  --path <exact-worktree-path> --bundles <archive-directory> --base <integrated-ref>
```

Archival refuses the current, dirty, detached, unintegrated, or unregistered worktree. It
creates and verifies a Git bundle before removing the worktree, and retains the branch.

## CI and promotion

Pull requests and pushes run the repository gate on Ubuntu and Windows. Changed milestone
directories additionally create a private self-hosted qualification matrix. Each job runs the
isolated milestone gate; machine-local Gate leases coordinate Windows and Linux workers without
holding the runtime during static work. Reports and smoke logs are uploaded even after failure.

The versioned pre-push hook blocks `codex/*` branches, every ref containing changed
`smokes/<id>/` directories, and every direct `main` update unless the exact SHA has an
orchestrator receipt. This turns milestone worker output into a local handoff by default.
Unprotected coordinator/tooling pushes keep using the repository gate;
`WORLDLINE_PREPUSH_SMOKE=1` still requests the complete smoke sweep.

Repository policy must mirror this locally enforced rule in the hosting service: protect `main`,
require pull requests and required checks, restrict push/merge permission to the orchestrator
identity or team, and forbid force-push and deletion. Repository files cannot enable provider-side
branch protection by themselves.

Promote a batch only after the integration SHA is fixed, the cross-platform repository jobs
pass, and the required runtime tier passes. Update release/version/changelog/roadmap files
once per train rather than once per worker milestone.
