# Evidence Explorer

The Evidence Explorer is a deterministic, read-only projection of Worldline's
tracked qualification evidence. Build it from the repository root with:

```text
java tools/harness/Gate.java --evidence-explorer
```

The command validates and reads these authoritative sources:

- `smokes/qualification.lock`
- `smokes/qualification-evidence/*.proof`
- `smokes/train-reconciliation.lock`
- each tracked `smoke.properties`
- `coordination/swarm/dispositions/*.properties`

It atomically writes canonical JSON and a dependency-free static dashboard to
`.worldline/reports/evidence-explorer/`. Rebuilding an unchanged tree produces
byte-identical output. The JSON exposes every pin's provenance, train class,
fingerprint, observation digest, proof-envelope digest, and last exact execution
head when the train records one. It also exposes supervised dispositions and
orphan proof envelopes.

The Explorer is not a verdict authority. It cannot promote evidence, alter a
qualification lock, or replace `Gate.java`. The canonical projection excludes
ephemeral local executor receipts, leases, and machine state. The separate live
overlay visualizes those inputs without changing the canonical snapshot.

## Local live overlay

Build the explicitly ephemeral overlay with:

```text
java tools/harness/Gate.java --evidence-explorer-live
```

This command first rebuilds the canonical files, then writes `live.json` and
`live.html` beside them. The live JSON binds the canonical `evidence.json` by
SHA-256 while keeping `ephemeral_state_included=true` and
`verdict_authority=false`. It contains only sanitized executor telemetry and
failure clusters, observational campaign performance signals and regression
triage, lower-only admission state, aggregate FIFO lease-queue state, the
current read-only cache decision for every smoke, and validated local links
from campaign receipts to native execution plans, receipts, metrics, and output
manifests. It reports linked, orphan, partial, and dangling native evidence,
then verifies campaign workload-log digests and counts referenced, orphan, and
missing logs. The performance table shows complete median windows or explicit
insufficient history. Its triage table projects deterministic investigation
candidates for increases and zero baselines, but fixes automatic execution,
verdict authority, and admission effects to false.

The live overlay also inventories the bounded local captures retained after a
failed `ExecutorServerCycle` cell. It recognizes only the closed
`server-qualification-failure` family and canonical legacy/Rust capture names.
Each row exposes the backend, workload, complete/partial and orphan state,
aggregate byte count, and content identities for the fixed artifact kinds.
Capture paths and artifact content are never serialized. Unknown files,
non-directories, links, excess files, and over-budget captures cannot become a
valid row and are counted as malformed local diagnostics. These identities are
diagnostic only; they are neither behavioral evidence nor transport permits.

The overlay also groups failures from linked native campaign runs. Each stable
cluster identity binds its conservative failure class, campaign, backend,
executor version, termination, exit code, and ordered executor-request
identities. Executor containment observations such as timeouts, truncation, or
escaped descendants remain in the executor-infrastructure domain. A nonzero
workload exit is deliberately labeled `unclassified-workload`; the Explorer
does not infer an oracle mismatch, a provider fault, or an infrastructure
failure from an exit code. Orphan native runs remain visible in link counts but
cannot enter a campaign cluster. These clusters are observational and have no
verdict or admission authority. The overlay exposes
identities and aggregate measurements only; local paths, process IDs, argument
vectors, environment values, and log content remain excluded.

Each cluster also produces one bounded, review-only follow-up item. Rebuild the
standalone plan with:

```text
java tools/harness/Gate.java --campaign-follow-up
```

Executor-infrastructure clusters suggest `repeat-exact-request`; unclassified
workload exits suggest `prepare-minimization-review`. The item selects only the
lexically first request identity, limits the suggestion to one attempt, and
requires review. The schema fixes automatic execution, queue mutation, verdict
authority, and admission effect to false. It contains no command, path,
environment value, process ID, or log content.

Live output changes with local machine state and observation time. It is useful
for diagnosis and scheduling, but it is never pinned, transported as behavioral
evidence, or consumed as a qualification verdict.
