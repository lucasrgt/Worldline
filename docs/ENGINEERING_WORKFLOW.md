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
| `WORLDLINE_GATE_WAIT_SECONDS` | 7200 | Lock acquisition timeout |

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

After merging and reconciling the candidates in a clean committed staging worktree, create the
push authorization:

```text
java tools/harness/Gate.java --orchestrator
WORLDLINE_ORCHESTRATOR_PUSH=1 git push <remote> <staging-or-main-ref>
```

The orchestrator gate requires a verified `integration-plan.json`, proves that every candidate
SHA and the declared base are ancestors of the current commit, runs the complete repository
gate, and writes `.worldline/reports/orchestrator-push.json`. The pre-push hook checks the receipt,
integration-plan digest, and exact local SHA. Setting the environment variable without a matching
receipt cannot authorize a push. Any new commit invalidates the authorization.

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
