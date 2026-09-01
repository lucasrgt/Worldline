# Worldline Infrastructure Program

Status: canonical coordination plan; implementation is in progress.

This document preserves the complete program for evolving Worldline's local
verification infrastructure into a native, hermetic, explainable, and
eventually distributed system. It also records the deferred design direction
for the Worldline Package Manager (WPM). WPM begins only after the execution,
evidence, and distribution foundations described here are qualified.

## 1. Outcome

Worldline keeps Java as the authority for repository policy, behavioral
semantics, official-oracle comparison, qualification, release evidence, and
public APIs. A new Rust executor owns operating-system process mechanics,
isolation, resource enforcement, and execution receipts. Remote services are
added only when more than one real worker host exists.

The target flow is:

```text
Gate / Runtime Fabric / IntegrationTrain (Java policy)
                         |
                  execution plan
                         v
             worldline-executor (Rust)
                         |
        Job Object / cgroup / namespace / Docker
                         |
              JVM smoke and Minecraft
                         |
                 execution receipt
                         v
       Java validation / proof / pin / release authority
```

Later, without changing the authority boundary:

```text
IntegrationTrain / orchestrator
              |
       authorized job set
              v
      optional coordinator
       /       |        \
 Windows GUI  Windows   Linux
    worker    headless  worker
       |         |        |
  local Gate + local official artifact + local lease
       |
  signed execution receipt and content-addressed outputs
```

### 1.1 Implementation ledger (2026-08-31)

The durable plan was established on branch
`codex/worldline-infrastructure-program` at commit `69059c072`. The native
mechanism is developed separately in `worldline-executor`; Worldline shadow
policy remains isolated on branch `codex/executor-shadow-contract`. No native
backend is yet a qualifying or default runtime lane.

Completed foundation slices:

- execution plan, receipt, and observational metrics v1 contracts, canonical
  JSON identity, atomic outputs, a direct backend, and hostile fixtures in
  executor commit `59fb054`;
- Windows Job Object containment, limits, accounting, bounded capture, owner
  death, timeout, root-exit cleanup, and executor-death cleanup in `9b69be7`;
- Linux cgroups v2 containment, pre-exec assignment, pidfds, an independent
  cleanup watchdog, memory-plus-swap, CPU and task limits, accounting, and
  empty-cgroup proof in `4ee1d50`;
- explicit separation of Windows process units from Linux process-plus-thread
  task units in `e3b269c` and explicit `peak_tasks` telemetry in `5a204b2`;
- strict Java plan production and receipt verification plus a real Windows
  legacy/Rust differential in Worldline commit `8e3f53324`;
- Linux cgroup plan, receipt, telemetry, and delegated-scope shadow verification
  in `b6103da5c` and explicit task-accounting verification in `3aecd368f`.
- unprivileged Linux user/network namespace enforcement for `network=none`,
  with inherited-versus-isolated loopback invariants, in executor commit
  `e251645` and Worldline shadow commit `e6f203728`.
- concurrent-job receipt independence and interrupted-executor atomic-output
  invariants in executor commit `1fbafe4`;
- Linux Landlock read-only/read-write allowlists, positive and negative path
  fixtures, and a JVM shadow probe combining cgroup, no-network, and filesystem
  controls in executor commit `2bc7bca` and Worldline commit `51d1536a5`.
- observational Linux `memory.max`, OOM-kill, task-limit, and CPU-throttling
  counters with positive limit-event fixtures in executor commit `ca68dad` and
  strict Java telemetry verification in `48df107a6`.
- canonical content-addressed stdout, stderr, and metrics manifests in executor
  commit `7d438cc`, with independent Java byte, order, size, digest, and receipt
  binding verification in `ce069be3f`.
- an immutable local SHA-256 object store with streaming publication, atomic
  create-if-absent semantics, corruption checks, fresh materialization, and
  convergent concurrent writers in executor commit `25205aa`. This is a local
  CAS primitive only and does not itself authorize remote transport.
- a permit-bound loopback HTTP object transport in executor commit `8573cda`.
  Strict policy-produced manifests authorize exact digest-and-size objects for
  both upload and hydration; every byte stream is rehashed through the local
  store, requests are bounded, publication is idempotent, and the service
  never asserts PASS. This is a local qualification boundary, not permission
  for remote network deployment.
- a fail-closed Java artifact transport catalog and content validator in
  Worldline commits `7a0f7a2b5` and `7d8aecce7`. Only canonical tracked smoke
  pin envelopes are remotely eligible; module, test, stage, smoke, observation,
  and diagnostic caches remain local-private, while official and mapped
  runtimes are explicitly forbidden.
- deterministic Worldline permit generation in commits `6e3ba4f87` and
  `5845de037`. `Gate --cas-permits` emits only digest and size for artifacts
  that pass the content validator and belong to the exact tracked-file set;
  untracked candidates, repository-root escapes, and duplicate digests fail
  closed.
- an exact, versioned Rust CAS protocol handshake in executor commit
  `fa9bdcb`. `worldline-cas protocol` identifies the permit schema and
  loopback transport without inference, while `serve` publishes one flushed,
  machine-readable ephemeral listen address before accepting requests.
- an explicit Java-to-Rust transport shadow in Worldline commits `cc7f3c7a5`
  and `f789b210f`. `Gate --cas-shadow` verifies the exact Rust protocol,
  generates policy permits, uploads one authorized tracked proof, hydrates a
  fresh store, materializes it, and independently repeats Worldline content,
  class, size, digest, and byte-equality validation. It never starts Minecraft,
  mutates the canonical cache, or treats transport success as behavioral PASS.
- canonical portable module-cache bundles in Worldline commits `f3248d873`
  through `6c623cc6a`. The binary envelope binds module name, module-input
  digest, Java toolchain, sorted normalized class paths, sizes, per-class
  digests, and exact bytes. Pack, validation, and extraction reject links,
  traversal, non-class payloads, truncation, trailing data, corrupt content,
  toolchain drift, and replacement of an existing immutable target.
- fixed-length Rust hydration for objects above the HTTP library's 32-KiB
  chunking threshold in executor commit `372887f`. The server preserves an
  exact `Content-Length` response instead of expanding the strict client to a
  second framing mode; a 64-KiB round-trip fixture permanently covers the
  previously untested boundary.
- a canonical Java-owned module transport index in Worldline commits
  `5033fa0d7` and `f961565f4`. It maps the logical tuple of module name,
  module-input digest, and Java toolchain to one opaque bundle digest and size.
  Strict parsing, canonical encoding, bounded cardinality, unique logical
  keys, closed fields, and artifact-policy validation keep lookup authority in
  Java while Rust remains a dumb blob service.
- a secret-free `worldline.smoke-input-graph.v1` read model and
  `Gate --input-graph ID` query in Worldline commits `423a322d3`, `16b9de40a`,
  and `c49b6af20`. The graph remains shadow-only and the legacy fingerprint
  remains authoritative.
- read-only `Gate --cache-explain ID` and
  `Gate --input-dependents KIND:KEY` queries in Worldline commits `9fc37a523`
  and `6c653b5e3`. Cache explanations preserve the exact local-proof-before-pin
  lookup order and distinguish the predicted decision from a clean
  current-tree receipt.
- a source-only `Gate --input-diff ID@GIT_REF` query in Worldline commits
  `814aa6b45` through `9e51cf43d`. It applies the current graph engine to an
  exact tracked Git snapshot, verifies both graph roots against the legacy
  fingerprint engine, and reports added, removed, or changed node identities
  using hashes only. Untracked local artifacts, official JARs, source content,
  environment values, and secrets never enter the snapshot or output.

Evidence recorded for these slices:

- Rust format, Clippy-with-warnings-as-errors, and complete workspace tests pass
  on Windows and passed on Ubuntu under WSL2 at the preceding executor
  checkpoints;
- the containment suite passed ten consecutive Linux runs before the unit-name
  correction, then passed completely on both platforms after the corrected
  schema;
- live Java-to-Rust probes pass through Windows Job Objects and through a
  delegated Linux `systemd-run` scope;
- local CAS format, Clippy, round-trip, corruption, overwrite, and eight-writer
  convergence fixtures pass on Windows and Ubuntu under WSL2;
- permit manifests reject unknown fields, duplicate digests, oversized
  objects, and non-loopback clients; HTTP fixtures prove idempotent upload,
  fresh hydration, digest and size revalidation, denied-object isolation, and
  no publication after corrupt input. The exact executor commit passes the
  complete Windows workspace and, from a native WSL ext4 clone, the complete
  Ubuntu format, Clippy, and workspace suites including cgroups, Landlock,
  limits, and `network=none`. Running the Landlock fixture directly from the
  Windows-mounted `/mnt/c` DrvFS tree fails at ELF execution with `EACCES`; the
  same exact commit passes from ext4, so Linux qualification must use a native
  filesystem rather than treating DrvFS as a supported Landlock mount.
- artifact classification rejects unknown families, directory escapes,
  restricted families, unknown policy fields, and binary payloads renamed as
  proof envelopes; the exact clean Worldline branch passes the canonical Gate
  (`cold 138151ms < 240000ms`) with 624 current train proofs and zero pending.
- the exact permit-generation branch emits 624 sorted smoke-proof objects,
  carries no paths or behavior content, reports no CSM RTW deviations, passes
  NYA, and passes the canonical Gate (`cold 181197ms < 240000ms`) with zero
  pending train proofs.
- the exact Java-to-Rust shadow transported and revalidated the authorized
  object `sha256=440c21f7aaeae78cf0771594589cf05299f4af1aa6509511fe8599cd5306f709`
  at 225 bytes, left zero entries under the shadow workspace after cleanup,
  and fails closed when the exact Rust binary or protocol is absent. Its clean
  branch reports no CSM RTW deviations, passes NYA, and passes canonical Gate
  (`cold 141899ms < 240000ms`) with 624 current train proofs and zero pending.
- the portable-module shadow transported the same 225-byte proof plus the
  current `api` module bundle, bound to module-input digest
  `e65b171660f90dd55ec26bd12836fe71631496b636972360fb990240dd62b690`.
  The 369086-byte bundle had SHA-256
  `1cdc497facb8590bef5d5c8942a9716e0feaea11a81491540f8c4bafbb65e0ff`,
  was hydrated into a fresh store, extracted, and compared class-by-class and
  byte-for-byte with the Gate-produced immutable module entry. The clean
  shadow repeated the exact hashes and left zero workspace entries.
- the exact Rust fixed-length commit passes format, Clippy with warnings as
  errors, and the complete workspace suite on Windows; the CAS format,
  Clippy, and suite also pass under Linux/WSL with an isolated target. The
  exact clean Worldline branch passes canonical Gate
  (`cold 149630ms < 240000ms`) with 624 current train proofs and zero pending;
  CSM checks seven ways with no deviations and NYA passes.
- the module-index shadow hydrates and revalidates the 298-byte index object
  `sha256=1420b88f692113c38e738d6577bd7dc448e32947cb389295ca941d2e2523a55d`
  before lookup. Its exact logical key selects the already qualified 369086-byte
  `api` bundle and no other object; extraction remains class-by-class and
  byte-equal, and cleanup leaves zero shadow entries. The exact clean branch
  passes canonical Gate (`cold 190406ms < 240000ms`) with 624 current train
  proofs and zero pending; CSM checks nine ways with no deviations and NYA
  passes.
- all 624 qualification fingerprints are complete, distinct, and byte-equal
  to independently assembled graph roots; graph JSON redacts environment and
  system-property values, provider and train transitions are sealed, CSM has
  no deviations, and the exact branch passes canonical Gate
  (`cold 141024ms < 240000ms`).
- cache explanation fixtures cover miss, disabled, verified-local, and tracked
  pin decisions; dependency queries cover paths, modules, and environment
  inputs, reject traversal, remain non-mutating, and pass the exact clean
  canonical Gate (`cold 143048ms < 240000ms`).
- cross-commit input comparison returns zero changes for an adjacent
  documentation-only commit and eleven changed module nodes for the historical
  `testkit-cycle` fixture at `44052f060`; unsafe traversal refs fail closed,
  temporary snapshots are deleted after success and failure, and the exact
  clean branch passes canonical Gate (`cold 159638ms < 240000ms`) with all 624
  dual graph roots equal and no provider or train proof pending. CSM RTW reports
  no deviations and NYA passes.
- the canonical Worldline Gate reaches `verify passed` with all content stages
  restored, but the host currently violates the unchanged hot latency SLO
  (`20361ms >= 7000ms` on the latest run). The budget was not weakened.

Current boundaries remain honest: Linux mount and PID namespaces,
loopback-only policy, Windows filesystem/network isolation, complete
hostile-fixture coverage, broader legacy differentials, and all qualifying
promotions remain pending. Artifact classification now covers tracked smoke
pin envelopes and generated Worldline-only module bundles; tests, stages,
smoke logs, runtime observations, sanitized diagnostics, official runtimes,
and mapped derivatives remain disabled or forbidden. Permit-bound local
transport, Worldline permit production, and explicit end-to-end shadows exist,
but automatic cache lookup, upload, hydration, authenticated remote deployment,
and S3 storage remain disabled. Module bundles now have a canonical logical
lookup index exercised end-to-end in shadow, but production adoption still
needs trusted index distribution, authenticated deployment, and integration
with the normal module-cache miss path. The input graph covers qualification
identities in dual mode, current-tree dependents, cache decisions, and
cross-commit node differences, but does not yet publish module cache identity.
A1 through A4, B3, and D1 through D3 and D5 are therefore in progress; A5 and
remote transport deployment have not started.

## 2. Program principles

1. `java tools/harness/Gate.java` remains the only qualification entry point.
2. Rust supplies mechanism and measurements; Java retains policy and verdicts.
3. Official artifacts, original assets, natives, mapped derivative classes,
   and decompiled sources are never uploaded to a remote cache or worker.
4. Every native boundary is introduced in shadow mode and earns promotion
   through differential or invariant evidence.
5. Execution telemetry never becomes frozen behavioral evidence.
6. Existing file leases remain the local last line of defense even when a
   remote coordinator exists.
7. Existing receipt and pin validation is reused before it is replaced.
8. Missing executors, schemas, identities, inputs, outputs, and capabilities
   fail closed for profiles that require them.
9. Runtime performance work receives stable optimization IDs before it can be
   enabled by default.
10. Each program increment is one isolated worktree and one reviewed logical
    commit. Global generated catalogs remain integration-train outputs.

## 3. Explicit non-goals

- Rewriting Gate, TestKit, Atlas, trace semantics, or Minecraft adapters in
  Rust.
- Loading Rust into Minecraft through JNI.
- Treating a remote worker or cache as proof that a behavior passed.
- Replacing official client or server behavior with a reimplementation.
- Sending official artifacts to a coordinator.
- Adding Go and Rust simultaneously before a persistent service is justified.
- Implementing WPM before the infrastructure program reaches its package
  manager prerequisite gate.

## 4. Stable internal contracts

The program introduces four versioned internal contracts. They are reviewed
structural capabilities, not milestone-numbered public TestKit identities.

### 4.1 `WorldlineExecutionPlan`

The canonical request from Java policy to the native executor declares:

- schema and job identity;
- exact argument vector without shell interpolation;
- normalized working directory;
- inherited, removed, and explicitly set environment variables;
- read-only inputs and writable output roots;
- stdout, stderr, metrics, and receipt destinations;
- timeout, memory, CPU, backend-explicit Windows process or Linux task, and
  disk limits;
- backend, network, and parent-death policies;
- expected tool and input identities;
- whether the job may access an official local runtime;
- the local lease identity that authorized such access.

The plan has a canonical UTF-8 encoding and a SHA-256 identity. Unknown fields
or unsupported schema versions fail closed.

### 4.2 `WorldlineExecutionReceipt`

The native executor returns:

- request digest;
- executor version and source identity;
- selected platform backend;
- exit code and structured termination reason;
- stdout and stderr digests;
- output-manifest digest;
- backend-labeled peak memory plus Windows process or Linux task count;
- CPU and I/O observations;
- timeout and limit events;
- descendant-cleanup outcome;
- parent and lease identity observations.

Unstable measurements are marked observational. They are excluded from
behavior fingerprints and portable proof roots.

### 4.3 `WorldlineInputGraph`

An explainable Merkle DAG names the files, trees, product modules,
dependencies, toolchains, artifact identities, environment values, runtime
lanes, and platform constraints that affect an execution. Java initially
builds and validates the graph. Rust may stream and hash graph nodes only
after exact differential coverage exists.

### 4.4 `WorldlineWorkerAttestation`

A future remote worker binds:

- worker key and host capability identity;
- authorized job nonce;
- exact source commit and tree;
- local Gate and executor identities;
- official artifact hashes observed locally, never artifact bytes;
- local lease acquisition;
- execution-plan and receipt digests;
- output object digests;
- cleanup and sandbox outcomes.

The orchestrator independently validates the attestation. A signature proves
which worker emitted a receipt, not that the behavior is correct.

## 5. Workstream A: native executor

### A0. Baseline and decision record

Deliverables:

- frozen census of current process launch, capture, timeout, cleanup, and
  platform-specific sites;
- latency and reliability baseline for tooling, server-headless, and Windows
  GUI lanes;
- stable optimization record for the native execution path;
- architectural decision recording the Java-policy/Rust-mechanism boundary;
- prohibited-artifact and remote-output classification table.

No production path changes in this increment.

### A1. Schema and hostile process fixtures

Create the execution-plan and execution-receipt schemas plus fixtures for:

- normal exit and nonzero exit;
- crash and abrupt parent loss;
- timeout races;
- child, grandchild, and detached-child attempts;
- output larger than pipe buffers;
- independent stdout and stderr ordering;
- Unicode and paths containing spaces;
- environment removal and injection;
- memory, process, CPU, and disk limits;
- concurrent jobs;
- partial receipt writes;
- interrupted executor shutdown;
- restart after an abandoned workspace.

Java owns a strict receipt verifier before any production integration exists.

### A2. Windows backend

Implement in Rust:

- suspended child creation;
- atomic Job Object assignment before resuming the child;
- kill-on-job-close;
- memory, CPU-rate, and active-process limits;
- parent-PID monitoring;
- timeout and tree termination;
- Job accounting and empty-job confirmation;
- exact argv handling without `cmd.exe`;
- bounded log capture and output hashing.

The first comparison target is the current C# Job Object runner. Restricted
tokens and AppContainer remain separate promotions.

### A3. Linux backend

Implement in Rust:

- cgroups v2 ownership;
- memory, task, and CPU limits;
- parent-death signaling;
- optional mount, PID, user, and network namespaces;
- read-only input mounts and explicit writable roots;
- loopback-only and no-network policies;
- cgroup accounting and empty-cgroup confirmation;
- bounded log capture and output hashing.

Bubblewrap may remain a temporary sandbox mechanism. Docker remains a
separately qualified fallback.

### A4. Shadow integration

Runtime Fabric gains an adapter that can run executor fixtures through both
the legacy and Rust backends. Shadow mode may observe and compare results but
cannot qualify releases. Required comparisons include:

- exit and termination classification;
- stdout and stderr bytes;
- descendant cleanup;
- applied limits;
- output manifests;
- path and environment behavior.

### A5. Tooling-cycle promotion

The first qualifying use is a structural tooling cycle that never starts
Minecraft. Rollback to the legacy backend is tested and recorded.

### A6. Server-headless promotion

Run legacy and Rust executions sequentially against fresh official-server
workspaces under the same canonical local lease. Require:

- equal normalized behavioral observations;
- no remaining descendants;
- exact output ownership;
- no unplanned network access;
- fresh independent worlds;
- preserved proof and pin validation.

### A7. Windows GUI promotion

Qualify native client and Aero lanes independently. Cover:

- OpenGL and Pbuffer lifecycle;
- Gradle homes and Aero prebuild;
- two-replica cycles;
- larger memory and CPU profiles;
- coordinator death;
- graphical child trees;
- framebuffer observation equivalence;
- driver and GPU provenance that remains outside portable behavior roots.

### A8. Default and retirement

Enable the Rust backend only after the rollback path and every applicable lane
pass. Retire the C# and shell launchers in a later reviewed increment, not in
the enabling change. Docker remains available when its stronger filesystem
boundary is required.

## 6. Workstream B: hermetic execution and input graphs

### B1. Environment manifest

Move from ambient inheritance to explicit environment allowlists. Record why
each inherited value is behavior-affecting, tool-affecting, or observational.
Secrets are never written to plans or receipts.

### B2. Filesystem manifest

Declare:

- tracked read-only source roots;
- immutable shared cache roots;
- official local artifact mounts;
- per-job temporary and output roots;
- forbidden parent and sibling paths;
- maximum output size and file count.

The executor rejects path escapes, reparse-point escapes, and unexpected
writes when the backend can enforce them.

### B3. Input graph v1

Compile current fingerprint inputs into explicit graph nodes. Preserve SHA-256
and portable Java token canonicalization. Run old and new fingerprint engines
in dual mode across the complete smoke catalog.

### B4. Cache explanation

Add commands that report:

- why a smoke was executed, reused, or pinned;
- which graph node changed between two commits;
- which smokes depend on a file, module, adapter, toolchain, or environment
  value;
- whether a change affects qualification, runtime observation, lane identity,
  or documentation only.

### B5. Fingerprint promotion

The graph becomes authoritative only after the legacy and graph fingerprints
have a reviewed migration for every smoke. No bulk silent invalidation or
transport is allowed.

## 7. Workstream C: execution telemetry and adaptive admission

### C1. Structured telemetry

Collect peak resident memory, commit charge where available, process count,
CPU time, I/O, output volume, bootstrap duration, runtime duration, limit
events, and termination cause.

### C2. Historical profiles

Aggregate by lane, smoke ID, executor version, OS, architecture, and relevant
runtime capability. Measurements remain local or sanitized before upload.

### C3. Conservative admission

Feed observations into the existing adaptive learning barrier. Measurements
may lower admission immediately for safety. Increasing width still requires
the reviewed promotion sequence, repeated frozen waves, and unchanged
behavioral signatures.

### C4. Failure clustering

Group infrastructure failures by stable signatures without merging behavioral
divergences. OOM, timeout, escaped descendant, tool absence, provider failure,
and oracle mismatch remain distinct classes.

## 8. Workstream D: content-addressed transport

### D1. Artifact classification

Every cache family declares one of:

- `portable-worldline-source-derived`;
- `portable-behavior-proof`;
- `sanitized-diagnostic`;
- `local-private`;
- `official-or-derived-forbidden`.

Unknown classes cannot be uploaded.

### D2. Dumb blob transport

Add immutable `GET` and idempotent `PUT` operations keyed by SHA-256. The
remote service supplies bytes only; it never asserts PASS. Existing Java
validators hydrate and revalidate the local cache.

### D3. Cache lookup order

Use:

```text
verified local object
  -> verified tracked pin
  -> remotely hydrated object validated locally
  -> fresh execution
```

### D4. Backends

Support a local shared directory first, then an S3-compatible object store.
Authentication, encryption, retention, and audit logging are deployment
concerns. Object identity and local validation remain backend-independent.

### D5. Cache families

Adopt one family at a time: proof envelopes, normalized evidence, safe module
outputs, test receipts, stage receipts, then sanitized heavy diagnostics.
Mapped or official-derived artifacts remain excluded.

## 9. Workstream E: distributed execution grid

This workstream does not start until at least two real hosts with legitimate
local official artifacts are available.

### E1. Worker capability registry

Register OS, architecture, JDK, executor, isolation backend, memory, CPU,
official artifact hashes, GUI/OpenGL capability, GPU/driver provenance, and
qualified lanes.

### E2. Narrow worker agent

The agent accepts only signed, schema-valid Worldline job requests. It checks
out an exact authorized commit, verifies a clean tree, takes the local lease,
runs the local Gate through the Rust executor, and returns attestations and
allowed objects.

Candidate code is untrusted. Workers are disposable, unprivileged, denied
production credentials, and sandboxed from unrelated repositories.

### E3. Coordinator

The coordinator owns queueing, capability matching, retry classification,
job nonces, host health, and scheduling. It never owns behavioral verdicts.
Rust is the default implementation choice. Go may be introduced here if a
persistent service is demonstrably easier to maintain than the Rust service.

### E4. Dual lease

Remote scheduling permission and the host-local file lease are both required.
A network lease can never replace the local lease because manual local Gate
invocations must remain safe during coordinator failure or partition.

### E5. IntegrationTrain bridge

IntegrationTrain continues to validate branches, ownership, ancestry, and
candidate readiness. It emits the authorized job set, receives worker
attestations, and independently validates them before orchestrator
qualification.

### E6. Lane differential farm

Add cross-host Windows headless, Windows GUI, and Linux lanes only after each
host has local qualification. Platform portability is promoted through
reviewed differential evidence, never inferred from matching exit codes.

## 10. Workstream F: evidence explorer

Build a read-only evidence graph over releases, trains, commits, milestone
receipts, smoke fingerprints, observations, proof envelopes, pins, executor
receipts, migrations, dispositions, and worker attestations.

Initial output is static HTML and machine-readable JSON. It must answer:

- why a smoke ran;
- why it restored locally, remotely, or from a pin;
- which input caused a miss;
- which host and executor produced an observation;
- when a behavior last executed against an official runtime;
- whether evidence was executed, migrated, or carried;
- which receipts or heavy logs are orphaned;
- which queue or lease is delaying qualification.

The explorer is never an evidence source. It is a derived, rebuildable read
model.

## 11. Workstream G: distributed campaigns

Use the executor, input graph, CAS, and optional grid for:

- protocol-14 fuzzing;
- mutation coverage;
- deterministic worldgen seed shards;
- repeated instability campaigns;
- lane differentials;
- profiling and regression campaigns;
- failure deduplication;
- deterministic minimization queues.

Campaign roots deterministically derive shard seeds and case ranges. Java
continues to interpret behavior and divergences. Rust schedules, isolates,
measures, and transports permitted objects.

## 12. Source and repository strategy

The preferred implementation is a separate Worldline-owned repository:

```text
worldline-executor/
|-- crates/
|   |-- execution-schema/
|   |-- executor/
|   |-- platform-windows/
|   |-- platform-linux/
|   |-- telemetry/
|   |-- cas/
|   `-- agent/                 # deferred
|-- schemas/
|-- fixtures/
|-- tests/
`-- release/
```

Worldline pins source commit, schema versions, release version, and platform
binary digests. The default static Gate does not require Rust. Runtime profiles
require the pinned executor only after promotion. Source builds remain
available for development and release reproduction.

## 13. Deferred program: Worldline Package Manager

The package manager begins only after Workstreams A through D are qualified.
The grid and evidence explorer may proceed in parallel with WPM, but WPM must
reuse the execution, input graph, CAS, provenance, and transaction contracts
rather than inventing weaker alternatives.

### 13.1 Product identity

Working name: **Worldline Package Manager (WPM)**.

`WPM` is preferred while the manager is governed by Worldline's fail-closed
compatibility and evidence model. `Lazuria Package Manager (LPM)` remains a
possible later product name if the tool becomes ecosystem-neutral and its
governance separates from Worldline. Renaming must not change package IDs or
lockfile semantics.

### 13.2 What WPM is

WPM is not a replacement for Gradle. It combines concepts from Cargo, Go
modules, a modpack resolver, and an instance transaction manager:

- Gradle or another build tool compiles source.
- WPM resolves released mods, loaders, runtime dependencies, patches, and
  compatibility constraints.
- WPM produces a complete immutable lockfile.
- WPM materializes a client, server, or development instance transactionally.
- Worldline can verify the resolved instance and publish compatibility
  evidence.

### 13.3 Files

An authored manifest:

```toml
[package]
name = "example-pack"
version = "1.0.0"

[runtime]
minecraft = "b1.7.3"
side = "client"
java = "8"

[loader]
stationapi = "2.0.0-alpha.6.2"

[dependencies]
"modrinth:project-id" = "=version-id"
"github:owner/repository" = { release = "v1.2.0", asset = "mod.jar" }
"wpm:aero-model-lib" = "^3.0"
```

A generated lockfile records no floating decisions:

```toml
schema = "wpm.lock.v1"
root = "sha256:..."

[[package]]
id = "wpm:aero-model-lib"
version = "3.0.0"
source = "github:owner/repository"
source_revision = "..."
file = "aero-model-lib-3.0.0.jar"
sha256 = "..."
size = 123456
license = "..."
loader = "stationapi"
side = "client"
```

### 13.4 Registry model

The WPM registry is a signed metadata index, not a mandatory blob mirror. It
records:

- stable package ID and aliases;
- upstream project identity;
- available versions and channels;
- exact downloadable files and hashes;
- supported Minecraft versions;
- loader and side variants;
- required, optional, incompatible, and embedded dependencies;
- provided capabilities;
- conflicts and replacement relationships;
- Java requirements;
- license and redistribution classification;
- installation phase and patch ordering;
- Worldline compatibility evidence references;
- yanked or compromised versions.

Package blobs remain at their authorized upstream source unless mirroring is
explicitly permitted.

### 13.5 Source adapters

WPM supports source adapters with one normalized internal model:

1. **Modrinth**: project/version metadata, loaders, game versions,
   dependencies, environments, file URLs, sizes, and hashes.
2. **GitHub Releases**: tagged releases and selected release assets. A tag or
   repository archive alone is not assumed to be an installable mod.
3. **WPM registry**: curated metadata overlays and packages whose upstream
   does not expose sufficient machine-readable metadata.
4. **Maven repositories**: loader and library artifacts where repository
   metadata and licensing allow use.
5. **Local path/file**: development-only packages, always locked by content
   digest and never silently published.

Every adapter handles API versioning, redirects, rate limits, disappearance,
and mutable upstream metadata. The lockfile always wins after resolution.

### 13.6 Why an overlay registry is necessary

Legacy Beta mods frequently have:

- no semantic versioning;
- no dependency manifest;
- jar-in-jar or base-class patches;
- order-dependent installation;
- implicit ModLoader, Forge, Babric, or StationAPI requirements;
- client-only behavior not declared in metadata;
- releases spread across forums, GitHub, and mirrors;
- incompatible combinations discovered only by users.

WPM therefore cannot delegate resolution entirely to Modrinth or GitHub. Its
registry supplies reviewed compatibility facts while preserving upstream
provenance.

### 13.7 Resolver

The solver understands:

- exact and semantic version constraints;
- release channels;
- Minecraft version and runtime side;
- loader capabilities and exclusivity;
- Java constraints;
- required, optional, incompatible, and embedded edges;
- virtual capabilities such as `loader-api` or `rendering-api`;
- variants of one package for different loaders;
- ordered legacy patch phases;
- user-selected conflict policies;
- yanked versions and security blocks.

Resolution is deterministic. Equivalent inputs and registry snapshots produce
byte-identical locks or the same structured failure.

### 13.8 Transactional instance materialization

WPM creates a staging instance, verifies every download, applies an explicit
installation plan, runs structural checks, optionally runs Worldline doctor or
smokes, and atomically promotes the instance. Failure leaves the prior instance
unchanged.

The executor provides process isolation. The CAS provides immutable downloads.
The input graph identifies the instance. Receipts explain every mutation.

### 13.9 Legal and distribution boundary

- WPM never downloads or distributes official Minecraft artifacts as package
  dependencies.
- A legitimate local launcher or user-provided artifact remains responsible
  for Minecraft ownership.
- Upstream files are downloaded directly when permitted.
- WPM does not mirror an asset without a license or explicit permission that
  allows redistribution.
- A project page is not permission to redistribute every historical file.
- License, source URL, file digest, and permission classification are preserved
  in the lock and SBOM.
- Unknown licensing fails closed for public pack publication, while a local
  user-directed download may be represented only when the upstream terms and
  source permit it.

### 13.10 Commands

The eventual CLI surface may include:

```text
wpm init
wpm add PACKAGE[@CONSTRAINT]
wpm remove PACKAGE
wpm resolve
wpm lock --check
wpm install
wpm update [PACKAGE]
wpm doctor
wpm verify
wpm graph
wpm why PACKAGE
wpm audit
wpm sbom
wpm export prism
wpm worldline test
```

`wpm install` never updates the lock implicitly. `wpm update` changes selected
constraints and produces a reviewable lock diff.

### 13.11 Development integration

A Gradle plugin may read `wpm.lock` to expose resolved artifacts to a mod
development build, but Gradle remains the build engine. WPM can submit the
resolved dependency graph to hosting security APIs and export SPDX or CycloneDX
SBOMs without making those external systems part of resolution.

### 13.12 WPM prerequisite gate

WPM implementation starts only when all of the following are true:

- execution plan and receipt schemas are stable;
- Rust executor is default for at least one runtime lane;
- content-addressed safe object transport is qualified;
- input graphs can explain instance-affecting changes;
- prohibited-artifact upload checks are executable;
- transactional workspace fixtures pass on Windows and Linux;
- Worldline can verify a package-manager-created fixture instance without
  weakening official-artifact policy.

## 14. Verification matrix

Every increment must declare its authoritative evidence before implementation.
At minimum:

| Boundary | Required evidence |
| --- | --- |
| Execution schema | Canonical round-trip, unknown-field rejection, digest fixtures |
| Process launch | Legacy/Rust differential fixtures |
| Timeout and cleanup | Descendant and parent-death invariants |
| Resource limits | Platform-native limit fixtures and metrics |
| Tooling promotion | Structural capability milestone |
| Server promotion | Fresh official-runtime behavioral differential |
| GUI promotion | Two-replica native framebuffer/runtime differential |
| Input graph | Complete-catalog dual fingerprint comparison |
| Remote CAS | Corruption, poisoning, and forbidden-upload tests |
| Remote worker | Authorized-commit, local-lease, signature, and sandbox tests |
| Evidence explorer | Rebuild equality against authoritative files |
| WPM resolver | Deterministic solver corpus and conflict fixtures |
| WPM install | Transaction, rollback, provenance, and instance verification |

Before each code change, run `csm context` for its scoped goal. Before each
handoff, run `csm check`, the applicable candidate or milestone Gate, and the
canonical Gate required by the engineering workflow. Runtime work must use
`Gate --runtime` or the applicable milestone Gate and must never invoke a
smoke runner directly.

## 15. Program completion criteria

The infrastructure program is complete only when:

1. the Rust executor is the qualified default for applicable local lanes;
2. legacy native launchers scheduled for retirement are removed;
3. hermetic plans and explainable input graphs are authoritative;
4. structured telemetry drives only reviewed adaptive admission;
5. safe cache objects can be transported and revalidated remotely;
6. at least two real hosts prove the grid, or the grid is explicitly closed as
   unnecessary by a reviewed decision;
7. the evidence explorer rebuilds from authoritative sources;
8. distributed campaigns preserve deterministic sharding and Java semantic
   authority;
9. every boundary has differential or invariant evidence;
10. all applicable CSM, source, dependency, Gate, runtime, and milestone checks
    pass on exact clean commits.

Only after the WPM prerequisite gate may the package manager move from this
preserved design into its own implementation program.
