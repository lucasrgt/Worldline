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

Evidence recorded for these slices:

- Rust format, Clippy-with-warnings-as-errors, and complete workspace tests pass
  on Windows and Ubuntu under WSL2;
- the containment suite passed ten consecutive Linux runs before the unit-name
  correction, then passed completely on both platforms after the corrected
  schema;
- live Java-to-Rust probes pass through Windows Job Objects and through a
  delegated Linux `systemd-run` scope;
- the canonical Worldline Gate reaches `verify passed` with all content stages
  restored, but the host currently violates the unchanged hot latency SLO
  (`20361ms >= 7000ms` on the latest run). The budget was not weakened.

Current boundaries remain honest: Linux namespaces, filesystem and network
isolation, full hostile-fixture coverage, broader legacy differentials, and
all qualifying promotions remain pending. A1 through A4 are therefore in
progress; A5 and later have not started.

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
