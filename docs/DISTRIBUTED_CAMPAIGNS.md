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
