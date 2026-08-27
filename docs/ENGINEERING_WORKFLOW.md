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
- `changelog/`;
- `README.md`;
- `docs/ROADMAP.md`;
- `docs/ARCHITECTURE.md`;
- `release/worldline.properties`;
- `modules/api/src/main/java/worldline/api/WorldlineVersion.java`.

The integration coordinator updates those files once for an accepted train. Frozen behavior
signatures live in each milestone's `smoke.properties`; the release manifest contains only
global identity, canonical inputs, core signatures, and the currently promoted milestone.
Before committing that train, the coordinator runs `java tools/harness/ReadmeStatus.java update`.
The release gate regenerates the release badge and bounded status table in memory and rejects
manual edits or stale counts.

All new branches use `codex/<kind>-<id>-<slug>`, where `kind` is `milestone`, `fix`,
`experiment`, or `train`. Milestone/fix candidates contain exactly one reviewed logical commit
over the declared base. Reconciliation accepts only `train` branches. `experiment` branches are
never integrable; they remain conditional work governed by NWC until resolved or archived.
`WorktreeLifecycle audit` rejects a live experiment unless its commit contains an NWC-generated,
branch-bound deferment and `csm nwc check` validates its cue, owner, and expiry. Runtime fixture
or flake corrections on `fix` branches must add a scoped NYA scar in the same logical commit.

After qualification, record a portable handoff before the coordinator commits the train:

```text
java tools/integration/SwarmHandoff.java record --ref REF --base REF \
  --receipt .worldline/reports/milestones/ID.json --disposition qualified
java tools/integration/SwarmHandoff.java check
```

`java tools/integration/SwarmDashboard.java` generates
`.worldline/reports/swarm-dashboard.html` from the worktree audit, branch triage, portable PASS
pins, versioned handoffs, and latest Gate timing report.

## Recursive swarm improvement

Every Ox Alpha worker starts behind the fail-closed supervisor preflight in `SwarmLoop`. The
supervisor supplies the exact base SHA, latest census, immutable wave-closure report, and one
`SwarmMicroWave` receipt. Preflight requires an exclusive clean
worktree at that base, reads this document and `AGENTS.md` completely, runs `csm context`, and runs
`csm nya recall` for the milestone, `tools/smoke`, and `modules/testkit`. It emits a PASS report only
when recall presents `NYA-01M0VSCA8F3WSMVW32R9XME7DQ`. The versioned Ox Alpha prompt forbids nested
task, explore, or subagent delegation because the launcher does not supervise nested work.
Top-level OpenCode workers are supervised as a micro-wave: each owns one worktree, every ID appears
in the receipt, and their Candidate phases may execute concurrently. `AdaptiveParallelism` starts
at four, caps width by live CPU and free-memory capacity, grows to the measured maximum after a
clean barrier, and falls to one after recurrence, dirty/stranded state, or systemic failure.
Official milestone runtime remains serialized by the existing cross-platform lease.

After preflight, invoke the worker through `OxAlphaLauncher` in `checkpoint` phase. The launcher
binds one reviewed OpenCode model, denies nested OpenCode task delegation, places the
positional worker message before variadic prompt attachments, and preserves immutable attempt logs
and a launch receipt. Direct `opencode run` invocations are not supervised evidence. After the
worker stops at its checkpoint, run `SwarmLoop pre-candidate` and resume the same recorded session
through the launcher's `qualify` phase.
The reviewed model set is exactly GLM 5.3 Flash, DeepSeek V4 Flash, and DeepSeek V4 Pro. GLM 5.3
Flash is the default high-concurrency Ox Alpha profile. The supervisor may set
`WORLDLINE_OX_ALPHA_MODEL=opencode-go/deepseek-v4-pro` for bounded correction work after a
recurrence or systemic failure, while DeepSeek V4 Flash is the fast alternate. Retired and arbitrary
model IDs fail closed before process creation.
Every launch supplies `--control-base` with the exact orchestrator SHA that authorized its checks.
The launcher rejects an authorized milestone base or worktree HEAD that does not contain that SHA,
so an older worktree cannot pass supervisor readiness and then execute a stale Candidate Gate.
If controls advance after a retryable attempt, first preserve its evidence archive, move the same
branch and worktree to the new clean base, and run `OxAlphaControlMigration`. It verifies the old
receipt, archive hash, milestone ID, session, and ancestry, then repeats CSM context and both
supervision and control-base recall before issuing the replacement preflight. Reapply the archived
checkpoint only after this PASS; the milestone ID and OpenCode session remain unchanged.
When a preserved checkpoint crossed an earlier control-base migration, pass its independently
sealed `--archive-base`, `--preflight-base`, and `--receipt-base`. Each must be an exact ancestor in
the new control base; they need not be linearly ordered because independently reconciled trains can
make them sibling ancestors. Collapsing different historical identities into one SHA fails closed.
The launcher closes the child's stdin pipe immediately after creation so non-interactive OpenCode
observes EOF and creates a session. Its self-test fails if a child can remain blocked on stdin.
After an archived selected-provider quota failure, the supervisor may set
`WORLDLINE_OX_ALPHA_FALLBACK=1` to resume the same receipt-bound session on the allowlisted free
profile. The launch receipt records both the profile and model; a new session or ID fails closed.
Fallback checkpoint resumes require a minimum 7200-second worker budget, and the source launcher
derives a larger outer timeout from that exact requested budget. This executable interlock prevents
large receipt-bound histories from being misclassified after the primary one-hour window.
After an archived systemic fallback-provider error, the supervisor may select whichever of GLM 5.3
Flash or DeepSeek V4 Flash differs from the primary model with
`WORLDLINE_OX_ALPHA_FALLBACK_MODEL`. DeepSeek V4 Pro is primary escalation, not a free fallback.
The launcher rejects models outside its executable
allowlist and records the exact selected model while preserving the same session, worktree, and ID.
When a completed Candidate or Milestone Gate tool result has a nonzero exit, the attempt is terminal.
The launcher permits 30 seconds for the worker to emit its disposition, then terminates only that
OpenCode process tree and records `supervisor_stop=terminal-gate-failure`. Repository diagnosis and
correction occur in the next supervised attempt, preventing cheap workers from consuming unbounded
read steps or editing after frozen Gate evidence.

Before Candidate Gate, the supervisor runs `SwarmLoop pre-candidate` against the same authorized
base and goal. The command rejects a CLI base that differs from the exact PASS preflight base/head
before recall or readiness can run. It recalls each applicable scar separately, rejects stale generated narratives,
lane-census drift, semantic maps missing the exact descriptor signal, packed control bodies, long
smoke lines, scaffold markers, and imports from a sibling milestone's private smoke sources. The
same readiness phase executes the Candidate smoke-statement ceiling before freezing its manifest;
a source over 150 statements cannot consume an attempt and then edit behind a stale interlock.
It validates every `symbols.map` row against the exact hashed `mappings.tiny`, including independent
empty client and server columns. New supervised data-driven cycles declare `expected.trace`; its
SHA-256 must equal `expected.signature`, the exact trace must appear in `MAP.md`, and runtime must
emit that bound signature. Flowing-water freeze fixtures may not notify neighbors while installing
their moving-water control before the initial still/moving pair has been observed.
Passive-animal spawner fixtures must declare a grass substrate in the exact trace and construct it
through the observed placement boundary before readiness passes. Retargeting a saved spawner on a
stone-only floor is an unreachable Packet24 oracle and may not consume the runtime lease.
packed-control detector balances nested condition parentheses before inspecting the body, preventing
valid braced controls from being rejected as packed source. The
semantic-map comparison recalls `NYA-01M0YSJXNA3TK6FHQW4QJ5RJZ5`. The interlock compiles the
runner, adapter, module, smoke, and official `oracle-src` source closures before it writes a
content-bound readiness report. Oracle compilation uses the verified official artifact without
starting runtime, so checked-exception and obfuscated-signature mismatches cannot consume a
Milestone attempt. For `DataDrivenCycle`, scenario compilation exposes only the modules declared by
`cycle.compile.products`; compiling against every repository module is a false closure proof.
Candidate Gate on a supervised milestone branch requires that PASS report and
rejects any later source change.
Candidates with smoke sources recall `NYA-01M0YH9M17ETMZA0F5X7981K4P`; the compilation result,
not textual inspection of imports or `throws` clauses, is the objective closure proof.
Candidates changing `modules/api` also recall `NYA-01M0YRVA4DD24Y22AHJQP2X3MF`. Exact closure
compilation preserves the release declared for every module; the Java 8 API may not adopt a
Java 21 language feature merely because TestKit declares release 21.
Packed-source prevention includes complete constructors, methods, and control bodies on one physical
line, including empty constructors. Minecart-collision readiness additionally rejects remotely placed
stone support geometry after the repeated pre-oracle reach failures recorded in NYA. It also rejects
`B173MinecartBooster.push`: the qualified parallel-rail booster does not prove that Packet7 initiates
a mover in a same-rail collision fixture, which requires a separately qualified bounded primitive.

After every worker, the supervisor preserves a verified Git bundle, binary patch, untracked source,
logs, receipts, and a SHA-256 archive before recording a final disposition. `QUALIFIED` requires a
real public contract, no scaffold marker, one clean logical commit, an exact milestone receipt and
portable handoff. `RETRYABLE` retains the same branch, worktree, session, evidence tail, and bounded
attempt count. `REJECTED` retains exact oracle, fixture, or instability evidence and names an NYA
scar. A worker without a disposition is `STRANDED` and is converted immediately to `RETRYABLE` or
`REJECTED`; a draft scaffold is never a handoff or train candidate.
The canonical census resolves each exact qualification against the base recorded in its receipt,
not against the latest wave control SHA. A registered rejection is terminal only when its tracked
disposition, archived commit/tree, worktree identity, and external archive digest all validate. The
wave base is a fallback solely for worktrees that have no exact receipt or explicit disposition.
Comparable telemetry may be carried from an archived baseline census only when both the milestone ID
and exact HEAD match. A changed or missing HEAD makes first-pass and recurrence unknown; an exact
tracked disposition may override matching baseline fields, and the new census records the baseline
SHA-256 so metric continuity is independently auditable.

At each micro-wave barrier, the supervisor aggregates equivalent causes across all completed workers,
records or updates each NYA scar exactly once, runs `csm nya check`, updates the base prompt when a
new scar applies, and verifies that the same scar did not recur before releasing another micro-wave.
A new micro-wave is blocked while the census contains dirty or failed workers,
an unowned retry, a rejection without a scar, a failed recall, a non-exact handoff, or a scaffold
presented as a contract. `SwarmLoop close-wave` emits an immutable report bound to base, HEAD, tree,
current and prior census hashes. It retains dispositions, first-pass rates, per-milestone corrections,
scar recurrence, pre-Candidate/pre-runtime prevention, rejection classes, revalidation, semantic
duplicates, receipt median/p95, safety counts, Pareto causes, deltas, and a moving window. Every
applicable scar must map to a versioned executable check or an explicit owned exception. If rates do
not improve, a contained executable process correction is mandatory before the report can release
another micro-wave. An archived `RETRYABLE` may enter the lateral queue only when its exact session,
owner, archive SHA, attempt and remaining attempt limit are present; it reduces adaptive width to one
but does not stop unrelated pristine candidates in the same cohort. A subsequent 25-candidate wave
remains blocked until all 25 current contracts are terminally qualified and integrated or explicitly
rejected with a registered scar and archive.

Before opening workers, create exactly one supervised receipt whose width cannot exceed the report's
live capacity:

```text
java tools/integration/SwarmLoop.java plan-micro-wave \
  --census <current-census.json> --closure <wave-closure.json> --base <exact-sha> \
  --id <candidate> [--id <candidate> ...]
```

The planner writes exactly one immutable receipt whose canonical path is derived from the closure
SHA-256. Alternate paths and a second receipt for the same learning barrier fail closed.

Each worker passes that closure and receipt to `SwarmLoop preflight`. Rejected semantic identities
are checked both before editing and before Candidate. Revalidation is permitted only for the same ID
after the registry carries an objective harness, fixture, or oracle change hash; a replacement ID is
never a workaround. `NYA-01M0YZVBKBPB0SB3CJYVQSPNA9` records the M674/M660 recurrence and routes
it to the versioned `rejected-semantic-exclusion` check.

The preflight distinguishes optional CSM store initialization from recall failure. A `csm context`
exit code of one is accepted only when stderr consists exclusively of the known uninitialized
WTW/RTW/NWC messages and the NYA section contains the mandatory supervision scar. `csm nya recall`
must still exit zero and present every required scar.

The scheduled private workflow runs differential fuzzing and mutation-manifest exploration only
after the canonical Gate. `NightlyQualityCampaign` splits a hard wall-clock budget between both
child JVMs, kills timed-out process trees, and publishes seed, volume, duration, status, and logs
under `.worldline/reports/nightly-quality/`. The `quality` dispatch profile reproduces it manually;
ordinary local and pull-request gates retain only deterministic short tests.

Smoke source policy counts semicolons outside comments, strings, and character literals instead
of rewarding physical-line packing. New runners and scenarios are limited to 300 and 150
statements respectively. `quality/smoke-statement-debt.properties` is a path-specific ratchet for
reviewed legacy overages: an allowance can decrease but cannot grow, move to a new file, or remain
after the source reaches its limit. Packed-line totals and maximum line density are independent
non-growth ratchets. Reusable family helpers live under `smokes/shared/<family>` and must be
declared through each consumer's fingerprinted `shared.inputs`; orphan helper families fail the
gate. Product, ordinary verification, and adapter `tokei` ceilings remain unchanged.

Start a milestone with `java tools/harness/Gate.java --new-milestone m<number>-<slug>`.
The command creates a deterministic descriptor, fail-closed cycle source, combined claim/cycle
document, and semantic map without overwriting any path. For narrative schema 1,
`smoke.properties` is canonical: `MilestoneNarrative` renders the combined document and the gate
rejects any manual drift. The draft has a content-derived signal and SHA so `Gate --candidate ID`
can validate its complete topology immediately, but its runner always fails runtime qualification
until the author replaces it with real evidence.

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
timeout, descendant cleanup, and `.worldline/smoke-logs/` output. The compatibility launcher
`Verify.java` was removed in the 2026-08-23 Fable 2 consolidation train; callers must enter
`Gate.java` directly. `--smoke-id` remains an alias for `--milestone` for old automation.

`timeout.seconds` is the Gate-owned outer cycle bound. A multi-arm runner uses a distinct,
milestone-owned key such as `child.timeout.seconds` for each subprocess so a valid sequence of
bounded children is not truncated and shared supervisor inputs remain stable.

Runtime Fabric workers also enter through the exact `Gate.java --milestone ID` command. The
pool owner first runs the static Gate once, holds the shared official-runtime file lock, and
issues a short-lived capability bound to its PID, repository root, clean head, tree, and lock
path. A worker with that capability runs only the runtime phase; an unauthenticated attempt
cannot bypass the ordinary exclusive milestone flow.

Docker workers use the namespace-safe equivalent: the host freezes the clean head, tree, image
ID, and tracked-file inventory into a read-only mount, while the container Gate runs against the
immutable `/workspace` tree and a bounded writable tmpfs. The canonical Gate content-addresses
Runtime Fabric compilation and admission self-tests, so unchanged backends are not repeatedly
built. Nightly CI routes headless work to Docker and GUI work to Windows Job.

The milestone gate deliberately has two phases. Static compilation, contract checks, and
affected tests use the shared verification slots and may run across many worktrees. Only the
executable cycle queues for the official-runtime lock. This keeps the scarce runtime idle only
for real evidence execution, not for compilation.

Every executed cycle records attempts, failures, and duration under the ignored report tree.
The nightly workflow idempotently folds those observations into reviewed
`smokes/schedule.properties`. Cold plans order higher historical failure rate first, then shorter
average duration, then stable milestone ID.

A milestone that declares `performance.scene`, paired baseline/treatment arms, and a budget under
`quality/` is checked after its runtime log is sealed and before its receipt is published. Aero
complete-frame budgets compare treatment quantiles with a same-host, same-scene baseline using a
reviewed rational multiplier plus bounded scheduling slack; absolute nanoseconds never become a
portable behavior claim.

Shared verification slots and official-runtime leases use monotonic FIFO ticket directories.
Only the oldest live ticket may attempt the corresponding file lock; tickets owned by dead local
processes are pruned. The source-launchable lease owner holds every acquired lock while an
internal Gate phase compiles and verifies, preserving the one-command bootstrap on every OS.

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
under each worktree's `.worldline/`. Module outputs are links to the immutable cache: POSIX uses
symbolic links and Windows uses directory junctions. Cleanup removes the link without traversing
its target. Unit suites store input-addressed PASS proofs with hashed output evidence; unchanged
suites restore those proofs, while missing or altered evidence executes fail-closed.

The following environment variables are supported:

| Variable | Default | Meaning |
| --- | --- | --- |
| `WORLDLINE_CONTROL_DIR` | OS user runtime-data directory | Shared lock/cache directory |
| `WORLDLINE_RUNTIME_LOCK` | Auto-detected legacy swarm lock | Compatibility runtime lock |
| `WORLDLINE_VERIFY_SLOTS` | Half the CPUs, capped at 4 | Concurrent repository gates |
| `WORLDLINE_BUILD_WORKERS` | Half the CPUs, capped at 16 | Module DAG and smoke-runner compilation workers |
| `WORLDLINE_TEST_WORKERS` | 4 | Test compilation/execution workers |
| `WORLDLINE_TEST_TIMEOUT_SECONDS` | 180 | Per-test JVM timeout |
| `WORLDLINE_TEST_CACHE` | `on` | Set to `off` to execute every selected unit suite |
| `WORLDLINE_SELF_TEST_CACHE` | `on` | Set to `off` to rerun the nested Gate lock self-test |
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
normalizes Git text inputs to LF before hashing, so a clean CRLF checkout and a clean LF checkout
resolve to the same behavior identity. Binary inputs remain byte-exact. It
records the fingerprint, PASS status, and evidence hash for each explicitly pinned milestone.
A fresh clone may reuse a matching reviewed pin without downloading the heavy log. The lockfile
is deliberately outside every milestone fingerprint, so committing updated pins does not create
a circular invalidation.

Each pin records its provenance. `source=executed` means an immutable PASS proof was present when
the pin was written. `source=legacy-frozen` is reserved for the one-time migration of the
pre-cache baseline: it binds the current input fingerprint to the already-reviewed
`expected.signature` in the milestone descriptor. This distinction prevents a historical freeze
from being presented as a newly executed log.

`source=refactor-equivalent` is reserved for the reviewed data-driven coordinator migration. Its
entry is accepted only when `smokes/data-driven-migration.lock` binds the deleted coordinator's
source hash, prior behavior fingerprint and evidence hash to the validated current plan hash.
Changing a plan, shared runner, module, adapter or artifact invalidates the pin normally.

The v3 fingerprint migration changed identity only by applying that Git text normalization. The
one-time `--accept-legacy-smoke-baseline` transition preserves each reviewed v2 evidence hash and
provenance while recalculating its portable v3 identity; it does not claim a new runtime execution.

The v4 identity adds declared `shared.inputs` under `smokes/shared/`. Its reviewed migration moved
17 byte-identical Aero paging mixin copies into two explicit base/direct variants. The transition
preserves the prior evidence hashes and provenance because the compiled runtime source is unchanged;
the gate verifies every consumer route and rejects reintroduced copies.

The fingerprint includes the milestone directory, its runner, shared runner support when used,
official-artifact descriptors, referenced adapters and toolchains, referenced product modules
with their transitive module dependencies, the Java runtime, and the operating-system
architecture. Documentation or release-index edits outside those inputs do not invalidate
runtime evidence. A new milestone has no proof and runs by itself; a shared adapter or product
change invalidates the scenarios that consume it. Malformed, missing, or altered proofs are
cache misses and fail closed to real execution.

Runtime execution and qualification policy have distinct content identities. Before invoking
Minecraft, a milestone records its sealed log under an execution-only fingerprint containing the
runner, executable descriptor fields, runtime assets, modules, adapters, toolchains, Java and OS.
Expected signatures, semantic claims, Atlas/TestKit declarations, narrative documents and
performance budgets are qualification policy. Changing only that policy restores the immutable
runtime observation and validates it again under the new contract; it never carries forward a
PASS result. Missing, malformed or digest-mismatched observations execute the runtime again.

The report-era `SmokeLegacyImport` was retired after the repository migrated every reviewed pin
to tracked, content-addressed evidence envelopes. Reports from before the receipt cache are no
longer accepted as qualification input; a missing or corrupt envelope fails closed and requires
new execution evidence.

The cross-worktree module cache is immutable. A compiler may publish a new digest directory but
may never replace an existing one. All immutable cache families use the same versioned 20 GiB/
30-day policy and digest-adjacent usage markers. Inspect or bound modules, tests, runners,
receipts, observations and verification caches without breaking active worktree junctions with:

```text
java tools/harness/Gate.java --cache-doctor
java tools/harness/Gate.java --cache-gc
java tools/harness/Gate.java --cache-rebuild-drill
```

The legacy `--module-cache-*` names are aliases for the unified commands. GC takes each digest's
publication lock, rescans every registered module worktree link, and removes only unreferenced
entries selected by the shared age or size policy. A successful GC immediately repeats the
unified doctor, so cleanup cannot leave a corrupt cache unnoticed. The versioned cold-rebuild
drill creates an isolated empty control directory, runs the canonical static Gate, doctors the
result, and writes its elapsed time and cache census to
`.worldline/reports/cache-rebuild.json`. Its reviewed bounds live in
`quality/cache-rebuild-baseline.properties`; the nightly runtime workflow runs both the doctor
and drill so a cache portability or reconstruction regression is detected before a release.
The rebuild has its own token-authorized latency mode because it deliberately reconstructs
module, test, harness, and verification caches; ordinary cold/hot Gate SLOs remain unchanged.
The doctor reports live and historically dangling worktree links; GC fails if that dangling
census increases, while stale missing targets remain non-protective and can heal on rebuild.

Repositories whose reviewed full-suite runs predate both the cache and retained reports may
explicitly accept the frozen descriptor baseline once. This command requires a clean committed
tree, `pending.expected=0`, and a valid 64-hex `expected.signature` for every discovered smoke:

```text
java tools/harness/Gate.java --accept-legacy-smoke-baseline
git diff -- smokes/qualification.lock
```

The resulting `legacy-frozen` rows are reviewable trust declarations, not reconstructed runtime
logs. Any input change invalidates them normally, so the changed milestone executes on the next
smoke gate and can then be repinned with `source=executed`.

## Data-driven ordinary cycles

Ordinary server milestones declare `cycle.schema=1` and route through
`tools/smoke/DataDrivenCycle.java`. The plan names the official artifact, compiled source roots,
product classpaths, scenario main class, descriptor arguments and frozen output contracts. The
runner compiles once, executes two fresh workspaces through the shared EOF policy, compares the
two observations and validates the exact signal and signature. Milestones with custom process
topology, runtime builds, GUI control or other special orchestration retain explicit coordinators.

The one-time mechanical rewrite is reproducible with `Gate.java --migrate-data-cycles`. It accepts
only the frozen ten-line coordinator template with exactly one scenario main and three evidence
prefixes, requires a current pin before deletion, writes the declarative fields, and produces the
reviewable migration lock. The gate enforces a non-decreasing generic-milestone ratchet.
Any later shared-runner change invalidates all generic fingerprints. The explicit
`Gate.java --refresh-data-cycle-pins` transition is fail-closed unless at least one generic
milestone has a current freshly executed proof; it preserves the other reviewed equivalence rows
and records hashes of the runner, plan and class-loader support boundary.

Exceptional coordinators enter the same bounded EOF policy through the public
`SmokeRetryBoundary`; the decision, backoff and telemetry remain owned by `SmokeRetry`. The
one-time EOF rewrite and its finalizer were removed in the 2026-08-23 compatibility-removal
train. Their immutable `smokes/eof-retry-migration.lock` and permanent gate check remain: the
gate rejects restored private helpers, fixed EOF sleeps, boundary drift or a carried proof that
no longer matches its source.

Pin the currently available, fingerprint-matching PASS proofs from a clean worktree explicitly,
then review and commit the lockfile. This may checkpoint a completed prefix after a later smoke
fails; the failing smoke is not available and cannot be pinned:

```text
java tools/harness/Gate.java --pin-smokes
git diff -- smokes/qualification.lock
```

When both a tracked carried proof and an intact local executed proof match the same fingerprint,
pinning prefers the executed proof and upgrades the provenance row. It never downgrades a current
execution to a migration or legacy declaration.

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
  m470-example=refs/heads/codex/milestone-m470-example \
  m471-example=refs/heads/codex/milestone-m471-example
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
  --reconcile full-integration=codex/train-full-integration
```

Reconciliation mode runs the complete smoke gate on that clean commit before
writing the qualified plan; it cannot combine multiple candidates. Train pin migration preserves
the immediate predecessor's source order. A source whose current digest did not change must retain
the predecessor ancestor count and ordered ancestor digests exactly; only a changed source may
advance its ancestry. The migration self-test enforces this before generated locks are committed.
Pin migration starts only from a clean committed source tree, verified with `git status
--porcelain --untracked-files=all`; untracked sources cannot participate in the ancestry census and
therefore fail closed before any lock is written.
Before invoking reconciliation, resolve both refs and require `git rev-list --count
<base-sha>..<consolidated-ref>` to equal exactly `1`. Preserve a multi-commit staging branch and
create a new single-commit `codex/train-*` consolidation ref instead of rewriting or deleting it.
Run `IntegrationTrain --ready-only` before both the milestone plan and reconciliation. Its ordinary
readiness phase requires one clean registered worktree, a direct single-parent commit over the
base, no untracked files, and exactly one receipt-bound qualified handoff. Reconciliation readiness
also runs `Gate --train-readiness`, which checks the generated README and documentation catalogs
without starting official runtime. Any failure blocks creation or reuse of a train attempt.
Before freezing the consolidated ref, update the integration-owned release surfaces with
`java tools/harness/Gate.java --refresh-readme-status` and `java tools/harness/Gate.java
--refresh-documentation`; both generated checks must pass before reconciliation starts. Milestone
workers continue to leave these global indexes to the train.
When reconciled TestKit sources change, run `java tools/harness/Gate.java
--refresh-testkit-artifact-pins` on the exact clean committed train. That maintenance command
compiles and materializes the module outputs, packages TestKit, and seals entry-by-entry evidence.
Commit `release/testkit-artifacts.lock` before train pin migration; a predecessor artifact or
unmaterialized cache link is not release evidence.

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
java tools/integration/WorktreeLifecycleLauncher.java audit --base <integrated-ref>
```

The launcher compiles the exact lifecycle source closure before executing the audit. Launching
`WorktreeLifecycle.java` directly is invalid because source-file mode omits its sibling helpers.
The report at `.worldline/reports/worktrees.json` records existence, dirty state, ancestry,
and archive eligibility. `prune` is dry-run only. Archival is deliberately explicit:

```text
java tools/integration/WorktreeLifecycleLauncher.java archive \
  --path <exact-worktree-path> --bundles <archive-directory> --base <integrated-ref>
```

Archival refuses the current, dirty, detached, unintegrated, or unregistered worktree. It
creates and verifies a Git bundle before removing the worktree, and retains the branch.
After bundle verification it removes only `.worldline`, `tmp`, and `output` beneath the exact
validated worktree, then reports the private file/byte count and that those ignored artifacts are
not recoverable from the tracked-source bundle. Every successful removal runs the canonical
unified cache doctor before the archival command can report PASS.

Configure and audit clone-local Git performance and Windows path support with:

```text
java tools/integration/RepositoryMaintenance.java setup
java tools/integration/RepositoryMaintenance.java doctor
```

Setup enables the untracked cache and long paths, starts fsmonitor when supported, registers Git
maintenance, and writes a reachable changed-path commit graph. The doctor verifies those surfaces.
On Windows it also records a bounded current-path I/O probe and prints an optional administrator
Defender exclusion command; it never changes security policy itself.

Setup also installs the repository's `worldline-smoke-lock` merge driver. The versioned
`.gitattributes` route sends `smokes/qualification.lock` through a sorted three-way union: disjoint
pin additions/updates merge automatically, while two different changes to the same pin fail closed.

## CI and promotion

Pull requests and pushes run the repository gate on Ubuntu and Windows. Changed milestone
directories additionally create a private self-hosted qualification matrix. Each job runs the
isolated milestone gate; machine-local Gate leases coordinate Windows and Linux workers without
holding the runtime during static work. Reports and smoke logs are uploaded even after failure.

Hosted verification restores the immutable Gate cache with an OS- and Java-bound key. The shared
setup action also restores pinned `tokei` 14.0.0 binaries instead of recompiling the source counter
in every job. Superseded pull-request runs are cancelled; push verification is never cancelled.
Every Gate workflow renders `verify.json` stage timings into the job summary.

Test discovery also compares every tracked API `Remote*Test` class with `DomainApiTest`, so a
new helper cannot compile while remaining silently unexecuted. Tracked `__snapshots__/*.wlsnap`
files require a sibling `.owner.properties` file declaring `test.source` and `snapshot.name`; the
source must remain tracked and contain the corresponding literal `toMatchSnapshot` declaration.

The scheduled private runtime workflow starts with `Gate --smoke-plan`, sends only missing
server and GUI proofs through Runtime Fabric, qualifies tooling-only entries through their exact
milestone gates, and then runs `Gate --pin-smokes`. If the reviewed lock changes, automation opens
a lockfile-only pull request; it never writes proofs directly to `main`. TestKit release tags are
validated against the package version, exact generated artifact set, and generated SHA-256 file
before publication.

The versioned pre-push hook blocks `codex/*` branches, every ref containing changed
`smokes/<id>/` directories, and every direct `main` update unless the exact SHA has an
orchestrator receipt. This turns milestone worker output into a local handoff by default.
Unprotected coordinator/tooling pushes keep using the repository gate;
`WORLDLINE_PREPUSH_SMOKE=1` still requests the complete smoke sweep.

`tools/hooks/pre-push` and `tools/hooks/pre-push.cmd` are thin launchers for the same
`PrePushCheck` Java policy. Before reading Git's ref-update stream, Java compares the current
harness-source digest with `.worldline/gate/sources.sha256`; a missing or stale compiled policy
bootstraps the canonical Gate and restarts from the newly compiled classes.

Repository policy must mirror this locally enforced rule in the hosting service: protect `main`,
require pull requests and required checks, restrict push/merge permission to the orchestrator
identity or team, and forbid force-push and deletion. Repository files cannot enable provider-side
branch protection by themselves.

Promote a batch only after the integration SHA is fixed, the cross-platform repository jobs
pass, and the required runtime tier passes. Update release/version/changelog/roadmap files
once per train rather than once per worker milestone.
