# Distributed Campaign Contracts

Worldline campaign plans are Java-owned, canonical work partitions for bounded
exploratory jobs. They schedule work but never interpret Minecraft behavior or
confer qualification.

Generate the tracked nightly-quality plan with an explicit root seed:

```text
WORLDLINE_CAMPAIGN_SEED=173 java tools/harness/Gate.java --campaign-plan nightly-quality
```

On PowerShell, set the environment value for the command in the usual shell
form. `WORLDLINE_CAMPAIGN_SHARDS` may override the tracked default from 1 to
1024. The existing workload-specific case variables may change volumes within
their reviewed bounds.

`worldline.campaign-plan.v1` binds:

- campaign ID and root seed;
- sorted workload IDs, closed workload kinds, and total case counts;
- a bounded shard count;
- complete, disjoint half-open case ranges;
- a distinct SHA-256-derived signed seed for every workload assignment;
- Java semantic authority and observational execution authority.

The envelope publishes the SHA-256 of the exact canonical plan. Unknown fields,
noncanonical order, unsupported workload kinds, empty shards, duplicate
workloads, and altered derived seeds fail closed.

Each worker eventually returns a `worldline.campaign-shard-receipt.v1` bound to
the plan digest, exact Git head and tree, shard index, derived assignments,
result-object hashes, and optional native-executor request digest. Receipts are
explicitly non-authoritative. `CampaignReceiptSet` sorts them by shard index and
rejects missing or duplicate shards, mixed commits, changed ranges or seeds,
and incomplete case coverage. Its aggregate is therefore identical regardless
of worker completion order.

The first tracked spec covers protocol fuzzing and mutation exploration. The
same closed contract can describe world-generation, instability,
lane-differential, profiling, regression, and minimization workloads. Runtime
or behavioral campaigns must still enter through the applicable Gate and
official-runtime lease.

## Nightly shard execution

After the canonical Gate has produced the test classpath, execute one exact
shard with:

```text
java -cp .worldline/gate/classes NightlyQualityCampaign --budget-seconds 900 --seed 173 --shard-count 4 --shard-index 0
```

Workers may finish in any order. Each one atomically publishes the same
content-addressed plan, immutable workload logs, and one canonical shard
receipt under `.worldline/reports/nightly-quality/plans/<plan-sha256>/`.
The last worker to observe a complete receipt set writes `aggregate.json`.
Publication never replaces different bytes, and aggregation rejects gaps,
duplicates, changed assignments, or mixed Git trees.

Use `--plan-only` to print the plan without requiring compiled tests or running
either workload. A single-shard invocation remains compatible with the
scheduled workflow. Absence of `executor_request_sha256` explicitly means the
legacy Java launcher was used.

To isolate and measure the complete shard coordinator with the pinned Rust
executor, set `WORLDLINE_EXECUTOR` to its absolute executable path and run:

```text
java tools/containers/RuntimeFabricLauncher.java CampaignExecutorCycle --budget-seconds 900 --seed 173 --shard-count 4 --shard-index 0
```

The native cycle retains the canonical execution plan, receipt, metrics, and
bounded stdout/stderr under `executor-runs/`. The inner Java coordinator hashes
the exact plan it received and binds that request digest into its immutable
campaign receipt. The outer Java adapter independently verifies the Rust
receipt and all referenced output bytes before accepting exit zero. Java still
owns campaign partitioning, result interpretation, and aggregation; Rust owns
process containment, limits, cleanup, and observational telemetry.

After full receipt verification, the native cycle stores one sanitized local
telemetry sample under the `campaign` lane, `nightly-quality` workload, and
`campaign-worker` runtime capability. The content-addressed history contains
no argv, paths, environment values, or campaign log content and appears in the
live Evidence Explorer. Admission remains lower-only and reads exclusively
from `candidate-worker` samples; campaign observations cannot increase or
decrease worker admission.

## Structural instability shards

The second adapter repeats the deterministic Gate self-test without starting
Minecraft. Generate its plan with campaign ID `structural-instability`, or run
one bounded local shard after the canonical Gate:

```text
java -cp .worldline/gate/classes StructuralInstabilityCampaign \
  --budget-seconds 120 --seed 173 --shard-count 2 --shard-index 0
```

`WORLDLINE_INSTABILITY_CASES` controls the reviewed case volume. Each case must
exit successfully and emit the exact self-test success grammar; elapsed time is
normalized out of the result root and remains observational. The workload log
contains only the plan identity, assigned range and seed, executed/passed
counts, and a normalized root digest. A timeout or unexpected output publishes
a failed shard receipt and cannot masquerade as complete PASS coverage.

Use the shared native dispatcher by naming the adapter explicitly:

```text
java tools/containers/RuntimeFabricLauncher.java CampaignExecutorCycle \
  --campaign structural-instability --budget-seconds 120 --seed 173 \
  --shard-count 2 --shard-index 0
```

The Nightly command remains backward compatible without `--campaign`. The
Evidence Explorer correlates plans, receipts, workload logs, and native runs
from both report families while preserving one combined orphan analysis.
Every campaign descendant receives the same bounded 512-MiB JVM heap ceiling;
the outer Rust Job or cgroup limit remains responsible for aggregate memory.

## Official world-generation matrix replays

The third adapter replays the already-qualified
`m634-cross-lane-seed-matrix` official-runtime contract. It never invokes a
smoke runner directly: every replay enters through `Gate --milestone`, its
static preflight, and the serialized official-runtime lease. The M634 contract
continues to own the three Minecraft seeds, four chunks, observations, and
behavior verdict. The campaign seed partitions repeated executions of that
sealed matrix; it does not replace or reinterpret the Minecraft seeds.

After the canonical Gate and runtime profile pass, run one local replay with:

```text
java -cp .worldline/gate/classes WorldGenerationCampaign \
  --budget-seconds 1800 --seed 173 --shard-count 1 --shard-index 0
```

`WORLDLINE_WORLDGEN_MATRIX_REPLAYS` may increase the number of complete matrix
replays before sharding. Each bounded workload log contains only the plan,
range, derived campaign seed, replay counts, the existing M634 signature, and a
normalized result root. It contains no official JAR bytes, raw world, server
log, path, or runtime output. The campaign receipt remains non-authoritative;
only the nested Gate may accept the existing behavior contract.

The native form uses the same dispatcher and four-GiB containment boundary:

```text
java tools/containers/RuntimeFabricLauncher.java CampaignExecutorCycle \
  --campaign worldgen-matrix --budget-seconds 1800 --seed 173 \
  --shard-count 1 --shard-index 0
```

The runtime profile must already be valid in the exact worktree. A failed or
timed-out Gate publishes a failed campaign receipt, remains visible through
the native execution evidence, and cannot be aggregated as PASS.
