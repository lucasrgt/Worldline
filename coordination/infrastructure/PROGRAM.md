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

### 1.1 Implementation ledger (2026-09-01)

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
- an opt-in `ModuleBuild` hydration path in Worldline commits `3ae9d4ebb`
  through `3c9745a31`. `WORLDLINE_MODULE_CAS_MODE=shadow` requires an exact
  Rust protocol, loopback address, permit manifest, index path, and pinned
  index digest. A local miss consults the validated logical index, hydrates the
  permitted blob, validates bundle identity and every class, then publishes
  the immutable module entry without replacement. Disabled mode performs no
  CAS configuration or network work.
- a two-phase producer-side module publication boundary in Worldline commits
  `e1dcb331c` and `45b53eabc`. Java first prepares canonical bundles, the
  logical index, and one exact permit manifest without remote activity. Only
  after a CAS server starts with that immutable manifest may the second phase
  upload the prepared module objects and index. The transport shadow also
  proves that a locally stored object omitted from the plan is rejected by the
  remote service.
- an Ed25519 module-index trust envelope in Worldline commits `9adc71d4a`
  and `4185b9b8a`. The canonical envelope binds a release channel, publisher
  public-key digest, index digest, and index size. `ModuleBuild` now accepts a
  remote index only after Java verifies the configured public key, channel,
  envelope signature, exact bytes, and semantic index format. The signing key
  remains outside the repository and outside the hydration path.
- an opt-in immutable shared-directory module backend in Worldline commit
  `27e3260e1`. It reuses the signed index, bundle, publication-plan, and trust
  boundaries while addressing objects by the same SHA-256 layout as the Rust
  store. Concurrent publishers converge through atomic create-if-absent,
  readers reject links and corruption, and the normal `ModuleBuild` miss path
  remains responsible for local validation and immutable installation.
- a read-only sanitized shared-CAS inventory in Worldline commit `3be362322`.
  It correlates object identities with the signed module index and optional
  tracked smoke pins, detects missing signed references, corrupt objects,
  malformed layout, and valid orphans, and exposes no paths or content. The
  live Evidence Explorer embeds it only when the shared backend is configured.
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
- an exact `worldline.module-input-graph.v1` and
  `Gate --module-input-graph MODULE` query in Worldline commits `b12c1b4d4`
  and `793e4ee61`. `ModuleBuild` and the query now share one identity
  authority over module name, Java release and toolchain, dependency cache
  identities, normalized source paths, and exact source bytes. The read model
  exposes only paths and digests, and rejects unknown modules and cycles.
- authoritative qualification fingerprints in Worldline commits `63b8bb0fb`
  through `3acea88fc`. Normal cache, pin, receipt, and qualification consumers
  now receive the root produced by `worldline.smoke-input-graph.v1`; the former
  monolithic computation remains callable only by the catalog equivalence
  audit. Provider-discovery and train proofs were transported through their
  sealed migrations without changing any of the 624 fingerprint roots.
- the first qualifying native-executor use in Worldline commits `b3b7fd750`
  and `dfbb921a9`. `Gate --executor-tooling` runs the real Gate self-test first
  through the legacy Windows Job launcher, then through the Rust Windows Job
  backend, and finally through the legacy rollback path. Java remains the
  verdict authority, requires a clean exact commit and an explicit absolute
  executor, normalizes only the reported elapsed time, and cleans every
  transient output. The legacy launcher now waits for the Job Object to drain
  and records `processes.active=0` before publishing metrics.
- Worldline commit `d0f0f1fc1` extends that same qualifying tooling entry point
  with seven hostile legacy/Rust differentials. It covers normal and nonzero
  exits, independent stdout and stderr, Unicode and spaced argv, 256-KiB output
  beyond ordinary pipe buffers, cleared/injected environment state, a
  parent-child-grandchild timeout, and a successful run immediately after the
  abandoned tree is drained. Every pair agrees on termination, exit code, and
  exact selected-stream bytes; all jobs report zero active descendants. The
  hostile evidence digest is
  `66dd0e115049a4d7d190b25d9addf31a7c1bd532f8f8415beb9674459002b857`.
  The original Gate self-test and legacy rollback still pass with Rust request
  `f6a1b1e423bdac97ce94868b8f597ae22f008705442884c6627207c32b0d3e4d`.
  The exact canonical Gate passes cold (`113621ms < 240000ms`), CSM checks
  seven ways with no deviations, and NYA passes.
- Worldline commit `8709ff93c` adds the advanced hostile boundary matrix to
  the same structural tooling cycle. Legacy and Rust agree when an observed
  owner exits after the child announces readiness, when an active-process
  limit of one rejects a real child launch, and across four simultaneous
  legacy/Rust job pairs with isolated outputs and receipts. The advanced
  evidence digest is
  `570493952dde00db21e3f3f757291357014089c54e8174b49b0742258ec6cacc`;
  the complete tooling request is
  `f768ec0345d2963b050c1c6d9b1eaa1e533d7fb6c6117894081b4677205b3fbf`.
  Every contained job proves zero remaining descendants. The canonical Gate
  passes cold (`18808ms < 240000ms`) and hot (`2642ms < 7000ms`), CSM checks
  eight ways with no deviations, and NYA passes.
- Worldline commit `426e8e5b4` closes the remaining Windows tooling hostile
  boundaries. Legacy and Rust classify the same deterministic native crash,
  and abrupt death of either launcher leaves no metrics, manifest, receipt,
  atomic-output temporary, completed escape marker, or live descendant. A
  fresh Rust job succeeds immediately afterward. The interruption evidence
  digest is
  `e425b44e2f83296b50017eeb10f353406d4415e90113f9bdfafeae9102cf3d73`;
  the complete tooling request is
  `aaac3778291b31b22652446b957d5165f7b376a3667b24a0f112d1ee947caef1`.
  The canonical Gate passes cold (`126784ms < 240000ms`) and hot
  (`3121ms < 7000ms`), CSM checks seven ways with no deviations, and NYA
  passes.
- a secret-free `worldline.execution-environment-manifest.v1` in Worldline
  commits `38ee643ab` and `69797aede`. `Gate --executor-environment` exposes
  only variable names, set/inherit/remove modes, and behavior/tool/observation
  classifications. Values never enter the read model, secret-bearing names
  fail closed, and the qualified tooling cycle now clears ambient legacy state
  before applying the exact same Java-owned environment policy used by Rust.
- a `worldline.execution-filesystem-manifest.v1` and output-budget invariant in
  Worldline commits `3bcf9cec9` through `1ea3706ff`. The tooling profile
  declares source, temporary, and output roots, no official-artifact mounts,
  a 32-file/4-MiB ceiling, and all undeclared outputs as forbidden. Windows
  qualification is explicitly post-execution verification: recursive
  no-follow inspection rejects escapes, links, reparse points, special files,
  and budget excess without claiming filesystem isolation from a Job Object.
- atomic no-replace workspace promotion in Worldline commits `3188a0360` and
  `af7138e74`. The tooling cycle hashes the validated staging tree, performs a
  same-parent atomic move, rehashes the published tree, and publishes success
  only after source disappearance and digest equality. Existing destinations
  are never replaced; collision fixtures preserve both staging and destination,
  and a post-move mismatch attempts atomic rollback before failing closed.

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
- the configured miss-path shadow traverses the same `ModuleCacheRemote`
  function called by `ModuleBuild`, reports `hydrated module api from shadow
  CAS`, repeats the qualified index and bundle hashes, and leaves zero shadow
  entries. Pre-existing output is rejected rather than replaced, and Rust
  materialization output is trusted only after Java validates the pinned index
  and bundle bytes. The exact clean branch passes canonical Gate
  (`cold 172892ms < 240000ms`) with 624 current train proofs and zero pending;
  CSM checks ten ways with no deviations and NYA passes.
- the producer publication shadow prepares the exact existing 369086-byte
  `api` bundle and 298-byte index before server startup, binds them and the
  tracked proof to one permit manifest, uploads only the generated objects in
  phase two, rejects an unplanned object, and then traverses the configured
  `ModuleBuild` hydration boundary. It repeats the qualified hashes and leaves
  zero shadow entries. The exact clean branch passes canonical Gate
  (`cold 132273ms < 240000ms`) with 624 current train proofs and zero pending;
  CSM checks nine ways with no deviations and NYA passes.
- the signed-index shadow uses an ephemeral publisher solely as a trust
  fixture, hydrates the same 298-byte index through the dumb Rust CAS, and
  requires the configured `ModuleBuild` miss path to verify its Ed25519
  envelope before lookup. Self-tests reject a wrong key, wrong channel,
  changed signature, unknown envelope field, and corrupt index; the verifier
  also rejects symbolic-link inputs. The exact clean branch passes canonical Gate
  (`cold 148432ms < 240000ms`) with 624 current train proofs and zero pending;
  CSM checks nine ways with no deviations and NYA passes, and the end-to-end
  shadow leaves zero workspace entries.
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
- the `api` module graph resolves to
  `e65b171660f90dd55ec26bd12836fe71631496b636972360fb990240dd62b690`,
  exactly matching the already qualified module bundle input identity. The
  clean Gate restores every existing module and test cache entry rather than
  creating new identities, passes at `cold 138096ms < 240000ms`, and reports
  624 current train proofs with zero pending. CSM checks five ways with no
  deviations and NYA passes.
- the exact qualification-graph authority commit `3acea88fc` passes the
  canonical Gate (`cold 126816ms < 240000ms`) with all 624 fingerprints
  complete, distinct, graph-authoritative, and legacy-equal. Provider discovery
  carries 521 proofs with zero pending, the train carries 624 current proofs
  with zero pending, CSM checks ten ways with no deviations, and NYA passes.
- the exact executor-tooling proof commit `dfbb921a9` passes the canonical Gate
  (`cold 135022ms < 240000ms`) with 624 current train proofs and zero pending.
  Its explicit tooling profile then produces Rust request digest
  `47d48d60bda64a5aef26d95b205ca4a40a306bd083776eb68f3e81e2cca3f419`
  and normalized observable digest
  `458b7c9754023057b11ada8cc7b055c26c7ba3559f38d5a9f51d18106fbff32d`.
  Both legacy executions and the Rust receipt prove an empty contained process
  tree, output is complete and untruncated, the rollback executes after the
  Rust path, no official runtime starts, cleanup leaves zero cycle residue,
  CSM checks ten ways with no deviations, and NYA passes.
- the exact environment-manifest proof commit `69797aede` passes the canonical
  Gate (`cold 113046ms < 240000ms`) with 624 current train proofs and zero
  pending. The hermetic tooling differential retains normalized observable
  digest `458b7c9754023057b11ada8cc7b055c26c7ba3559f38d5a9f51d18106fbff32d`
  while the explicit environment changes the request digest to
  `ddec5b5536d3a6590e1daa14c6bd25a19423ef45dd4459afbabf44ad725691c8`.
  CSM checks ten ways with no deviations, NYA passes, and cleanup leaves no
  executor-tooling residue.
- the exact filesystem-manifest proof commit `1ea3706ff` passes the canonical
  Gate (`cold 153166ms < 240000ms`) with 624 current train proofs and zero
  pending. The final tooling request digest is
  `ce1be625afbc566c0984d9a5ed5101fa0f5478df7658b07ff5b7cfe13781714b`;
  the normalized observable digest remains unchanged at
  `458b7c9754023057b11ada8cc7b055c26c7ba3559f38d5a9f51d18106fbff32d`.
  CSM checks ten ways with no deviations, NYA passes, and no cycle residue
  remains.
- the exact transactional-workspace proof commit `af7138e74` passes the
  canonical Gate (`cold 123996ms < 240000ms`) with 624 current train proofs and
  zero pending. A clean Rust tooling run preserves normalized observable digest
  `458b7c9754023057b11ada8cc7b055c26c7ba3559f38d5a9f51d18106fbff32d`,
  atomically promotes and rehashes the complete bounded workspace, proves the
  legacy rollback path, leaves zero residue, and passes ten CSM ways plus NYA.
- Rust executor commit `803861d` publishes
  `worldline.execution-metrics.v2` with separately measured bootstrap and
  runtime durations. Windows and native-ext4 WSL suites pass formatting,
  Clippy with warnings denied, all workspace tests, Job Object tests, and the
  Linux cgroup, Landlock, network, limit, and process-tree fixtures. Worldline
  proof commit `523166a81` validates the phase partition fail closed and passes
  the canonical Gate (`cold 111419ms < 240000ms`) with 624 current train
  proofs and zero pending. Its clean tooling differential produces request
  digest `391964f76b844bba27f4b38a33680138b52f938177a701fdbed097520beeea8c`,
  normalized observable digest
  `458b7c9754023057b11ada8cc7b055c26c7ba3559f38d5a9f51d18106fbff32d`,
  transactional workspace digest
  `549eaf87c06244b563853527e9d092636ce13378c41bea002a9b90f102c70abd`,
  and a successful legacy rollback. CSM reports no applicable deviations and
  NYA passes.
- Worldline commit `5d75d057c` adds a closed, canonical, content-addressed
  local history for verified executor telemetry. Samples are grouped by lane,
  workload ID, executor version, backend, OS, architecture, runtime capability,
  and termination; rebuilt profiles retain duration, memory, process/task,
  CPU, I/O, limit-event, output-volume, and truncation aggregates while
  preserving unavailable platform metrics as `null`. The schema contains no
  argv, path, environment, stdout/stderr content, or official-artifact field.
  The exact canonical Gate passes (`cold 120345ms < 240000ms`) with 624 current
  train proofs and zero pending. A clean tooling differential produces request
  digest `9e93797a11fb674439de94e1a02eea098995a2e0cddd8990fa0e437f827468f1`,
  transaction digest
  `959ff65f7b553a46cc82976a62eb76d3197bca2d68a25bec1206bc331e8d2442`,
  and telemetry object digest
  `04218c510c455c202811010efb71bc32c36cec5ef308ef890de1b5ecee89893e`.
  CSM checks ten ways with no deviations and NYA passes.
- Worldline commit `192061dc5` connects host-matched `candidate-worker`
  profiles to the existing adaptive learning barrier through a strict
  `worldline.telemetry-admission.v1` contract. The observed peak receives a
  25-percent safety margin and can only raise the per-worker memory floor,
  which can only preserve or reduce admitted width. It cannot authorize an
  increase, bypass clean-wave promotion, or change serialized official-runtime
  admission. The exact canonical Gate passes (`cold 184073ms < 240000ms`) with
  integration tools compiled and self-tested, 624 current train proofs, and
  zero pending. A clean structural tooling run produces request digest
  `a152d2f9015dc4b9a459a3b9f2327368205fae7ffef013d61ff3ba091462bd38`,
  transaction digest
  `38139ead50c78ecfdc67bbd0a01703b976161a1d52c28b54e2ea6519af63d999`,
  and telemetry digest
  `52827a0f86abca93fde7fe9c2e86178457309dcd53a67533b2dc15fb374d6038`.
  That non-candidate sample correctly creates no admission floor. CSM checks
  eight ways with no deviations and NYA passes.
- Worldline commit `3947fa8e0` adds the canonical
  `worldline.execution-failure-clusters.v1` read model over validated,
  sanitized telemetry samples. Stable classes keep OOM, memory-limit,
  timeout, escaped-descendant, tool-absence, spawn/wait/owner-exit failures,
  provider failure, process crash, and oracle mismatch distinct; their
  domains also prevent executor infrastructure, provider infrastructure, and
  behavioral divergence from being merged. The exact canonical Gate passes
  (`cold 120299ms < 240000ms`) with 624 current train proofs and zero pending.
  A clean tooling run produces request digest
  `c87d7ce8791fe9dcacd3922738b1c5d2f52443de627b4be3648cc4febfa6e2e3`,
  transaction digest
  `82106fb418b337565862484c02d37b4eb58f0a8c38c024890e13525d2af5f0f4`,
  and telemetry digest
  `7b2e7e486d1d0e4083c80abc06c7f0be3c1d4b95ea0b936d035b16d1811c14d2`.
  The successful history rebuild reports zero failures and zero clusters.
  CSM checks nine ways with no deviations and NYA passes.
- Worldline commit `35e92b6f5` completes the canonical first Evidence
  Explorer slice. `Gate --evidence-explorer` validates all 624 tracked smoke
  descriptors against the qualification lock, proof-envelope bytes, and train
  reconciliation; it also projects 60 supervised dispositions and reports
  orphan proof envelopes. Rebuilds produce byte-identical canonical JSON and
  dependency-free static HTML at SHA-256
  `63c6de3b3065bfc49945325b136323c47606385ad9579bad3d05407481e630c9`
  and `684fcf208547df318476f5193c2d8158431527dad87ac14f1f60f75ed6c28ae6`.
  Tampered envelopes fail closed, unsafe text is escaped, zero orphan proofs
  are present, and the output declares itself non-authoritative and free of
  ephemeral machine state. The exact canonical Gate passes
  (`cold 95923ms < 240000ms`), CSM checks ten ways with no deviations, and
  NYA passes.
- Worldline commit `a72b1bd0a` adds an explicitly separate local live overlay.
  `Gate --evidence-explorer-live` first rebuilds and hashes the canonical
  snapshot, then projects validated executor telemetry profiles, stable failure
  clusters, lower-only admission state, aggregate FIFO queue health, and all
  624 current smoke-cache decisions into `live.json` and `live.html`. The
  projection excludes paths, PIDs, argv, environment values, and log content;
  it declares both `verdict_authority=false` and
  `ephemeral_state_included=true`. Queue fixtures distinguish live, abandoned,
  and partial tickets without mutating the queue, and telemetry fixtures retain
  two samples with one failure through the existing strict history parsers.
  The canonical snapshot remains byte-identical at
  `63c6de3b3065bfc49945325b136323c47606385ad9579bad3d05407481e630c9`.
  The exact canonical Gate passes (`cold 138942ms < 240000ms`), CSM checks
  nine ways with no deviations, and NYA passes. Live output hashes are
  intentionally not pinned.
- Worldline commit `7365d9a260` qualifies the first official
  `server-headless` Rust-executor differential against
  `m20-server-bootstrap`, using executor commit `803861d`. One canonical
  runtime lease covers a legacy Windows Job run followed by the Rust Windows
  Job run. Each side receives a fresh exact-commit disposable worktree, runs
  the structural Gate before runtime, materializes the already-local official
  server only under ignored `local/`, executes through `Gate --milestone`, and
  removes the complete cell afterward. Canonical evidence is byte-equal at
  digest `290897fd072b76d5d3d01ca8fe5e8a84586c25d4315fa77366c58533c8510cf7`;
  the Rust request digest is
  `6a8e33461a6e1b022e15265e26f06b8d68b6d53d2ef69c60077c9fd69a4de52f`
  and the sanitized telemetry object is
  `e72c78d81bee91ad9ce8f4f70d3911b9ce1efd21fdbaa08f7b98afadc163a601`.
  The 15,906-ms observation partitions into 15-ms bootstrap and 15,891-ms
  runtime, peaks at 809,144,320 bytes and seven processes, retains complete
  output without truncation, proves zero descendants, and rebuilds with zero
  failure clusters. The exact canonical Gate passes hot
  (`3506ms < 7000ms`), `Gate --runtime` passes cold
  (`6188ms < 240000ms`), CSM checks ten ways with no deviations, and NYA
  passes. Windows network policy remains honestly classified as inherited;
  the fixture observes an explicit loopback binding but does not claim OS
  network isolation.
- Worldline commit `e9ff4e55a` qualifies the real Windows Candidate profile
  against `m702-powered-rail-slope-propagation`, using executor commit
  `803861d`. Legacy and Rust Windows Jobs each receive a fresh exact-commit
  disposable worktree and an independent private build/test cache. Both run
  canonical `Gate --candidate`, execute all 41 module suites from a cold
  cache, drain every descendant, and produce the same canonical digest
  `4f7c075b61162663105b85601c09a0b9da4ef5caa0bfd1d7d665d4d5ba563bf8`
  over 127 Candidate-owned class files totaling 415,792 bytes. The Rust
  request digest is
  `e1d6302c6b0cd7c29a6c5884b43bae9347895fd61ecf4edddda86178c1fa6f1b`
  and the sanitized telemetry object is
  `adc060b3bac471febad93aef120bc49cc1a768820ba40b75661db530893f1797`.
  Its 1,368,981,504-byte peak produces a host-matched 1,711,226,880-byte
  floor; the configured 2-GiB worker estimate remains authoritative, proving
  live lower-only behavior without widening admission. The cold run also
  exposed and repaired an Atlas authority gap: the already-published
  `animation`, `lifecycle`, and `scheduling` subsystem contracts now generate
  the complete 196-unit coverage matrix instead of depending on stale cached
  test receipts. The exact canonical Gate passes cold
  (`104784ms < 240000ms`), CSM checks ten ways with no deviations, and NYA
  passes.
- Worldline commit `9b3bad608` qualifies the first official `gui-native`
  differential against `m10-native-render`, using executor commit `803861d`.
  One canonical runtime lease covers legacy then Rust Windows Jobs. Each arm
  receives a fresh exact-commit worktree and a bounded, no-follow private copy
  of the 3,871-file, 104,484,942-byte local b1.7.3 workspace. The canonical
  milestone launches four fresh Pbuffer JVMs per arm, proves mapped and
  official Tessellator frame equality, verifies mapped/JAR class provenance,
  keeps `Display` absent, and emits byte-equal evidence at digest
  `a7213d74d2086714e148c06f5cf06b7d083bd9dae1ca79321dbb1c9996cf0f14`.
  The Rust request digest is
  `837fb3ba1f8198eb366981e434f0e7e55f9e8b71c038e95aacbce049e2ef8050`
  and telemetry digest is
  `83afd7b5b04421dfae55c6dc6f8a61b63420a26db34da61ecf66ad2561557c6f`.
  Its 7,166-ms observation partitions into 12-ms bootstrap and 7,153-ms
  runtime, peaks at 741,785,600 bytes and seven processes, retains complete
  output, and proves zero descendants. The exact canonical Gate passes hot
  (`3355ms < 7000ms`), `Gate --runtime` passes cold
  (`5703ms < 240000ms`), CSM checks ten ways with no deviations, and NYA
  passes. Windows network remains `inherit`; GPU identity is diagnostic and
  excluded from portable evidence.
- Worldline commit `935d0fc41` qualifies the first pinned `gui-aero`
  differential against `m778-aero-culling-visual-integrity`, using executor
  commit `803861d` and Aero commit
  `091940b8e0cbd839169d8eb977992dfee83faff6`. Legacy and Rust Windows Jobs run
  sequentially under one canonical runtime lease. Each arm receives a fresh
  exact-commit Worldline worktree, an exact detached Aero worktree, fixed local
  official artifacts, and an empty private Gradle home. Both build Aero from
  source, launch the preparation client plus counterbalanced `off-on` and
  `on-off` clients, compare 24 full RGBA checkpoints, prove zero changed pixels,
  and reduce render work in both sessions. Their evidence is byte-equal at
  `76f09960fe0bebc3f7103b0d1004317883178299e3dae969f53ad867ce0cc796`.
  The Rust request digest is
  `b2b9a47acc5f3d3e9997a0a802a01a4cd945913735f00db252427239b021f4be`
  and telemetry digest is
  `59e119649f0e49f2e0f298561c7126ab1a1f98ef6b35402a95dadd974f8b9f04`.
  Its 787,205-ms observation partitions into 15-ms bootstrap and 787,190-ms
  runtime, peaks at 4,362,235,904 bytes and eleven processes, retains complete
  output, and proves zero descendants. PID-reuse-safe runtime capabilities now
  bind both PID and process start time. The exact canonical Gate passes cold
  (`107861ms < 240000ms`), `Gate --runtime` passes cold
  (`4818ms < 240000ms`), CSM checks ten ways with no deviations, and NYA
  passes. Both external and Worldline cells are removed after execution.
  Windows network remains `inherit`; the private Gradle homes intentionally
  use that network and are not evidence of network isolation.
- Worldline commit `31806d362` qualifies deterministic campaign contracts for
  protocol fuzzing, mutation, world generation, instability, differential,
  profiling, regression, and minimization workloads. Canonical plans divide
  every sorted workload into complete, disjoint half-open ranges and derive a
  stable signed seed for every workload-and-shard pair. Non-authoritative shard
  receipts bind the plan, exact Git head and tree, assignments, seeds, result
  hashes, and optional executor request; aggregation rejects incomplete,
  duplicate, mixed-commit, or changed-assignment sets independently of arrival
  order. The frozen three-shard fixture has plan digest
  `ebc7330c479f65db7e45204078cc3e418e4f32000bd9267fcdcf2eb1aa34286d`.
  The exact canonical Gate passes cold (`132955ms < 240000ms`), CSM checks nine
  ways with no deviations, and NYA passes.
- Worldline commit `c7859123d` adapts the real nightly fuzz and mutation
  campaign to those contracts. Each invocation executes exactly one shard,
  publishes an immutable content-addressed plan, bounded per-workload logs,
  and an atomic receipt, and emits an aggregate only after the complete set is
  present. A reverse-arrival two-shard run at seed 173 executes 8 fuzz and 20
  mutation cases: shard 1 leaves the set incomplete, then shard 0 completes two
  passing receipts on the same head and tree. Its plan identity is
  `a6e0adfb9fd03e263d56333cf3463188fb101a576b840c984df5c1fd1499c908`;
  the observed aggregate file digest is
  `a89216cb356a5bafdebf9c5cc03c142a183a48243045d1d5b2cafafc41c2950e`.
  The exact canonical Gate passes cold (`148102ms < 240000ms`), CSM checks nine
  ways with no deviations, and NYA passes. Elapsed values and aggregate bytes
  remain observational rather than behavioral evidence.
- Worldline commit `2effebae3` places the complete Java-owned Nightly shard
  coordinator inside executor commit `803861d`. The native plan binds the
  hermetic environment, UTF-8 process encoding, four-GiB Job limit, argv,
  output paths, and exact plan location. The inner Java coordinator hashes the
  received plan while the outer Java adapter independently verifies the Rust
  receipt, metrics, manifest, output bytes, exit, truncation, and descendant
  state. A clean single-shard run at seed 175 executes 8 fuzz and 20 mutation
  cases on exact head `2effebae310bad1067d48da714fa229248e29ec0` and tree
  `d8f04cc11f2fc644226e4e2b110e4e28aa5be6db`. The plan file, native receipt,
  and campaign receipt all bind request
  `f454e6d3a8740255c0faeba6eb1a902dbd9f3267fda114a55b5992872b2eb3bd`.
  Rust observes 641 ms, four processes, a 2,253,688,832-byte peak, complete
  output, and zero descendants; campaign plan identity is
  `641fbab90eed17d5ca861d5c19e3e98cb998dc9d0243709a7e09b53abd4ea97c`.
  The exact canonical Gate passes cold (`119270ms < 240000ms`), CSM checks ten
  ways with no deviations, and NYA passes. An earlier non-qualifying run exposed
  and retained a JVM reservation failure under a two-GiB limit; the reviewed
  four-GiB boundary is therefore measured rather than guessed.
- Worldline commit `01ac94d06` extends only the ephemeral Evidence Explorer
  overlay with independently validated campaign-to-executor links. It rehashes
  native plans, receipts, metrics, manifests, stdout, and stderr; correlates
  exact request digests with canonical campaign receipts; verifies every
  campaign workload-log digest; and counts linked, orphan, partial, dangling,
  missing, and unreferenced local objects without exposing paths, PIDs, argv,
  environment values, or log content. A clean seed-176 native campaign yields
  one linked Windows Job request
  `4b4ab00cb680b1a846011a8a0c186b1bd40c4be0f04a6ce18be44ff442857639`,
  two referenced workload logs, and zero objects in every error class. The
  native observation is 702 ms, 2,252,918,784 bytes, four processes, and
  executor version 0.1.0. The canonical Explorer remains byte-identical at
  `63c6de3b3065bfc49945325b136323c47606385ad9579bad3d05407481e630c9`.
  The exact canonical Gate passes cold (`126811ms < 240000ms`), CSM checks ten
  ways with no deviations, and NYA passes.
- Aggregate commit `0af4d260c` proves native multi-shard arrival without a
  grid. Seed 177 partitions 8 fuzz and 20 mutation cases into two shards;
  shard 1 arrives first and leaves aggregation incomplete, then shard 0 closes
  the exact head/tree set. The plan identity is
  `6fa40ece4f02444248e8b05c7ecc9b6bebae22cff1ae723d9820efa2f21f0821`.
  Rust requests `de756bdb56340d1a489220f2fca925526d723c4016c8e41f9b9a841d38dc3b9e`
  and `a56697498bbab61c0c72ed9f953a710be8bddd4725ddf796b433ae133f4da8c8`
  both exit cleanly with four processes and zero descendants; their elapsed
  observations are 630 and 598 ms. The final aggregate binds two passing
  receipts, 28 cases, and file digest
  `027d139b3e0825b3694cebbe51559c26acd4b11a93789db7c0107149830d26d4`.
  The live Explorer independently reports two linked runs, four referenced
  logs, and zero orphan, partial, dangling, or missing objects.
- Worldline commit `27e3260e1` completes the first D4 backend with a signed,
  immutable shared directory. Two concurrent publishers and an idempotent
  repeat converge on the same object, and the ordinary `ModuleBuild` miss path
  hydrates `api` at input digest
  `e65b171660f90dd55ec26bd12836fe71631496b636972360fb990240dd62b690`.
  The 369086-byte bundle remains
  `1cdc497facb8590bef5d5c8942a9716e0feaea11a81491540f8c4bafbb65e0ff`;
  extraction and repacking are exact. Corrupt objects, symbolic links, wrong
  trust, and unauthorized publication fail closed. The exact canonical Gate
  passes cold (`197469ms < 240000ms`), CSM checks nine ways with no deviations,
  and NYA passes.
- Worldline commit `3be362322` makes that backend inspectable without reading,
  hydrating, or publishing through it. The deterministic inventory distinguishes
  required signed index and module objects from optional recognized smoke pins
  and valid orphans, while reporting corruption, malformed entries, and absent
  signed references without paths or content. Hostile fixtures cover all four
  fault classes. The real `api` shadow finds exactly two verified objects, one
  signed index and one 369086-byte bundle, with zero faults and the unchanged
  bundle digest
  `1cdc497facb8590bef5d5c8942a9716e0feaea11a81491540f8c4bafbb65e0ff`.
  The exact canonical Gate passes cold (`142250ms < 240000ms`), CSM checks ten
  ways with no deviations, and NYA passes.
- Worldline commit `df7a35a9a` retains verified native campaign telemetry as
  sanitized local history under the distinct `campaign-worker` capability.
  Seed 178 executes 8 fuzz and 20 mutation cases through Rust request
  `8baeccfdc57fd21777bf23457d36b2d0734b2294cf6761874c7832f0c242024d`.
  Sample `434eea8a6146072b3e12060dd7a1eecf6e9f1ecd4d3d32d9f5af80bead09128b`
  records 1153 ms, 15-ms bootstrap, 2253049856 peak bytes, four processes,
  complete output, and zero failures in the live Explorer. Admission remains
  closed to `candidate-worker`, so the campaign creates no admission file. The
  exact canonical Gate passes cold (`143581ms < 240000ms`), CSM checks five
  ways with no deviations, and NYA passes.
- Worldline commit `9dc03ddce` qualifies structural instability as the second
  Rust-executed campaign adapter. The generic dispatcher selects either the
  existing Nightly fuzz-plus-mutation workload or deterministic Gate self-test
  cases while preserving the same canonical plan, shard, receipt, executor,
  telemetry, and aggregation boundaries. The adapter is explicitly structural:
  it executes no official Minecraft runtime and claims no behavioral coverage.
  An initial two-shard seed-180 run correctly failed when descendant JVMs tried
  to reserve about 960 MiB each inside the four-GiB Job. The two failed Rust
  requests are
  `5855e42974ed2f6c24b6f877fdfd17f62be1e9810cab83885fd526d7884f53bd`
  and `6de1ff22cea6054027ff5863e228fd44bfbed37c2486b90516d4746097751229`;
  their preserved local crash logs hash to
  `97f2b3976ea0419e8b7898504d4b468dd2345d29a4a53287714005d3039d8e28`
  and `331797f388116073e21bcf1790fc815980208dc73d7c523bab362f0f27c25606`.
  The controlled campaign environment now bounds each descendant JVM at
  512 MiB. A reverse-arrival two-shard seed-182 qualification then passes four
  cases at plan
  `59959f08d0553abcc50d9e160a9a28fca2fa154230c25a9fc757b9ed5c19bfc9`
  through requests
  `2e1afe6dd0a5e3ffd78956d0bd439a3c5c3d2ffbb7df0a9d8efa2ebb6e3a3d3b`
  and `541055fcd45a6ba7ee5c0899379bfa4ae40562bbce1b1cd39891ea2fb522d78f`.
  Their peak Job observations are 671682560 and 659865600 bytes with seven
  processes. A seed-183 Nightly regression still passes all 28 cases through
  request `c5b12f0115bd74a365ad169cc35d98df0c2a6910d56a6d5d5029ba9596851861`
  with a 228057088-byte peak. The live Explorer independently classifies the
  two historical failures with `exit_code=1` and the three successful runs with
  zero, without making the overlay authoritative. The exact canonical Gate
  passes cold (`116548ms < 240000ms`), CSM checks ten ways with no deviations,
  and NYA passes.
- Worldline commit `a3137efa7` qualifies the third campaign adapter by replaying
  the existing official `m634-cross-lane-seed-matrix` contract. Every replay
  enters through `Gate --milestone` and the official-runtime lease; M634 still
  owns the three Minecraft seeds, four chunks, solid-occupancy oracle, and
  behavior verdict. The campaign seed partitions whole sealed-matrix replays,
  and its receipt remains explicitly non-authoritative. An initial seed-184
  request `22d740fb5d7daa0afe0b3d6bc65a992d4f5bb6b31ebec5648552a34c07b00fad`
  failed in the static phase when the eight-process Job could not start another
  `javac`; it reached no Minecraft oracle. Rust observed 1361678336 peak bytes
  and 15 process identities. The measured worldgen profile now permits at most
  24 active descendants while retaining the four-GiB Job and 512-MiB JVM heap
  ceilings. Seed 185 then passes the unchanged M634 signature
  `e7e55219d0ff2afc770f220ab309cf77ad0b0ed92928b5dd5342ed7494e79576`
  at plan `02abb9edffd41dad928b95fc66965d66baf91f42c2bf576db626e11549080469`
  through Rust request
  `a030d25940429b494b9236cd94cfefe3f0a39910a88766bdc57b8c8a3d715ed5`.
  Sample `57dbfdf034af95a9977342d3d15199cf7e073ceaf1f64ec08197e4c80c0fe97d`
  records 85526 ms, 9-ms bootstrap, 1889628160 peak bytes, 25 observed process
  identities including the executor, complete output, and zero descendants.
  The live Explorer independently reports both linked runs and exactly one
  failure. The exact canonical Gate passes cold (`123902ms < 240000ms`),
  `Gate --runtime` passes cold (`5104ms < 240000ms`), CSM checks ten ways with
  no deviations, and NYA passes.
- Worldline commit `c759a36d2`, integrated at `ae3ceaf0a`, qualifies a sealed
  minimization replay adapter around `m9-scenario-minimization`. Every replay
  enters through `Gate --milestone m9-scenario-minimization`; M9 remains the
  sole authority for the 9-to-3 reduction, exact divergence, one-minimality,
  canonical artifacts, and public behavior signal. The campaign seed
  partitions whole M9 replays without changing the sealed scenario or
  evaluator seed, and the campaign receipt remains explicitly
  non-authoritative. Seed 207 exposed an eight-process Job limit before the
  oracle at 1,324,859,392 peak bytes and 15 observed process identities. After
  the measured full-milestone profile was raised to 24 descendants, seed 208
  reached compilation and failed closed on a missing prepared mapped workspace
  at 1,886,822,400 peak bytes and 23 processes. Seed 209 then passed plan
  `899327142a703f0949f72db11b70543eeb150129b1a5caeeeacecd5b97e46f32`
  through Rust request
  `d2f844052ad6b8c33d0bbeab6175d9c1dab9101d158368bb127e3570e7b556de`.
  Its telemetry object is
  `c7b87a3951a6710b46c4cb80ea88f8efd5b06732678474bf553be908c4a98356`;
  Rust observes 576,051 ms, 1,850,286,080 peak bytes, 23 processes, complete
  output, and zero descendants. The unchanged M9 contract signature is
  `1919dad99cf82004a5d6ea79fbf57b20aef2233da6a8d3e508cc91636f4a1cfd`,
  its public signal hashes to
  `25e48a7353598bd4e4e3847cda7b6a89e42be63936be1303028f3896c4e9ad85`,
  and the normalized artifact root is
  `cc85495e98aba1e802cd4c0e32093c5ab3993f7bb4b2370079886fd71a7126d6`.
  The live Explorer links all three native runs, exposes the two pre-oracle
  failures, and reports zero orphan, partial, dangling, or missing objects.
  The single passing history sample correctly yields `insufficient-history`
  and cannot affect admission. Nightly and structural profiles retain their
  eight-descendant limit; only full-milestone worldgen and minimization use the
  measured limit of 24. The exact slice passes `Gate --runtime` cold at
  178,664 ms and the canonical Gate hot at 3,523 ms; CSM checks ten ways with
  no deviations, and NYA passes.
- Worldline commit `f556e0f7b`, integrated at `467577afe`, qualifies
  observational regression triage over the current campaign performance read
  model. Each `increase` becomes one performance investigation candidate and
  each `zero-baseline` becomes one data-quality candidate. A stable case ID
  binds the metric, threshold, complete sanitized profile identity, and the
  ordered SHA-256 identities of every baseline and recent telemetry object.
  The strict Explorer parser recomputes that ID, requires every case to match
  one current signal with identical medians and ratio, rejects changed IDs or
  observations, and requires unique canonical ordering. The execution overlay
  and live Explorer advance to v3 and expose counts plus a bounded triage table.
  Both schemas fix `verdict_authority=false`, `admission_effect=false`, and
  `automatic_execution=false`: suggestions never create a queue, start a
  campaign, write a disposition, or assert a regression. Deterministic fixtures
  cover increases, zero baselines, ignored classifications, input-order
  independence, sanitization, changed IDs, and signal mismatch. The slice
  carries 520 train proofs, imports 98, executes six, and leaves zero pending.
  Its unchanged Gate first exposed external disk/CPU contention from another
  active `TrainPinMigration`: complete functional passes missed the cold and
  hot SLOs at 383,234 and 38,231 ms. Those reports are preserved outside the
  repository at SHA-256
  `8b313fa22ab95b9ffd2d22c7a9fca9143f0ab5231467dc77e131d02cc14de9d0`
  and `73c0464dec0c27da6f37f3cc78106e7f06b0af66c4f7550fe7bb8075a8ba0156`.
  After the concurrent migration exited, the exact same commit passes the
  canonical Gate hot at 3,729 ms. CSM checks eight ways with no deviations,
  and NYA passes.
- Worldline commit `85969eab4`, integrated at `14687948e`, qualifies
  conservative failure clustering over validated native campaign links. The
  live Explorer advances to v4 and exposes stable clusters whose identities
  bind the domain, class, campaign, executor profile, termination, exit code,
  and ordered executor-request digests. Timeout, crash, spawn, wait,
  owner-exit, output truncation, and escaped descendants remain explicit
  executor-infrastructure classes. A nonzero workload exit is instead fixed to
  `workload-nonzero-exit` in the `unclassified-workload` domain: the read model
  never guesses oracle mismatch, provider fault, or infrastructure fault from
  an exit code. Orphan native runs cannot enter a campaign cluster. The real
  minimization history contains two such failures, requests
  `00bd0dac938b0b68c95e0275cf9f858d6f5da7320d9c6d499b329a533de36158`
  and `195d6dca73aa163651711553d6b7a15472036c285cd07403b61e37807abe3de7`,
  deterministically grouped into cluster
  `410025f1422df82881e0172852aab55af6697c4312472a197f30c6a8cb168e41`.
  The observed result is two unclassified workload failures, zero
  infrastructure failures, and one cluster. Both the standalone schema and
  live projection fix verdict, admission, and automatic-execution authority to
  false. The exact slice Gate passes cold at 149,602 ms, carries 520 train
  proofs, imports 98, executes six, and leaves zero pending. CSM checks three
  ways with no deviations, and NYA passes.
- Worldline commits `91c32c04e` and `14bebceb6` repair a pre-existing Windows
  race in concurrent shared-directory CAS publication discovered by this
  qualification. Publishers now serialize each digest with a bounded local
  lock stripe plus a persistent cross-process file lock before the immutable
  move and read-only transition. Twenty repeated fixtures, each using two
  threads and four independent processes, complete with zero failures after the
  original code failed five of twenty thread-only rounds. The read-only
  inventory accepts only canonical zero-byte lock files under `locks/sha256`,
  excludes them from object counts, and classifies malformed lock namespaces,
  names, links, or content as faults.
Current boundaries remain honest: Linux mount and PID namespaces,
OS-enforced loopback-only policy, Windows filesystem/network isolation,
the broader server-headless matrix,
broader onscreen GUI/driver profiles, multi-Candidate histories, and default
runtime selection remain pending. The structural Windows tooling cycle and one official server
behavior/containment differential are qualified; neither makes Rust the
default backend. Artifact classification now covers tracked
smoke pin envelopes and generated Worldline-only module bundles; tests, stages,
smoke logs, runtime observations, sanitized diagnostics, official runtimes,
and mapped derivatives remain disabled or forbidden. Permit-bound local
transport, Worldline permit production, explicit end-to-end shadows, and an
opt-in signed shared-directory module backend exist. Automatic production
lookup outside that backend, authenticated remote deployment, and S3 storage
remain disabled. Module bundles now have a canonical logical lookup index, an
opt-in normal miss path, and permit-bound two-phase producer publication
exercised both in shadow and against the shared directory. Signed index
verification provides a cryptographic trust boundary, but broader production
adoption still needs protected envelope distribution, stable publisher-key
governance, authenticated deployment, audit policy, and an explicit promotion
from `shadow`. A sanitized read-only inventory and live Explorer projection
cover shared-store integrity, but they are not a durable deployment audit log.
The input
graph covers qualification identities in dual mode, current-tree dependents,
cache decisions, cross-commit node differences, and exact module cache
identities. Qualification and module fingerprints now use their graphs as the
authority while retaining legacy equality audits. A1 through A4 and D1 through
D3 and D5 are therefore in progress; A5 is qualified for the
structural Windows tooling and Candidate cycles. A6 now has one qualified official Windows
server behavior/containment slice, B1 covers tooling plus server-headless, and
the post-execution portion of B2 covers disposable server cells without
claiming write isolation. A7 now has qualified native Pbuffer and pinned Aero
visual-integrity slices. Native Windows filesystem/network enforcement,
broader onscreen/driver GUI profiles, full A8 promotion, broader runtime histories,
and remote transport deployment remain pending.

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

Status: the strict plan/receipt verifier and executor-owned fixtures cover the
schema and platform mechanisms. Worldline commit `d0f0f1fc1` adds qualifying
legacy/Rust coverage for seven observable process boundaries: normal/nonzero
exit, separate streams, pipe-scale output, Unicode/spaced argv, explicit
environment removal/injection, a three-process timeout tree, and restart after
that tree is abandoned. Commit `8709ff93c` adds owner loss, deliberate
active-process exhaustion, and four concurrent legacy/Rust pairs. Commit
`426e8e5b4` adds crash classification, abrupt launcher loss, atomic-output
absence, descendant containment, and clean restart. The enumerated Windows
hostile process boundaries are now qualified at the Worldline tooling layer.

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

Status: the basic live shadow and tooling promotion remain qualified, and
commit `d0f0f1fc1` adds a deterministic seven-scenario legacy/Rust matrix with
exact termination and output comparison. Timeout cleanup orders descendants
by ancestry depth, kills children before parents, confirms death, and proves a
subsequent clean execution. Commit `8709ff93c` proves owner-loss containment,
deliberate active-process exhaustion, and receipt/output independence across
four simultaneous legacy/Rust pairs. Commit `426e8e5b4` completes the Windows
matrix with crash and interrupted-executor atomicity differentials.

### A5. Tooling-cycle promotion

The first qualifying use is a structural tooling cycle that never starts
Minecraft. Rollback to the legacy backend is tested and recorded.

Status: qualified on Windows by `Gate --executor-tooling` at Worldline commit
`dfbb921a9`. The profile runs legacy baseline, Rust primary, and legacy
rollback sequentially against `Gate --self-test`; it requires byte-equivalent
normalized observations, bounded output, successful termination, and empty
process trees. This does not authorize a Minecraft runtime lane or default
backend selection.

Commit `d0f0f1fc1` extends the same qualifying command with the seven hostile
differentials before the original baseline/Rust/rollback sequence. The
extension remains structural and never starts Minecraft.

Commit `8709ff93c` follows those fixtures with owner-loss, process-limit, and
four-pair concurrency differentials. It remains structural, runs no official
runtime, and preserves the original legacy rollback after every added check.

Commit `426e8e5b4` then proves deterministic crash classification and abrupt
launcher-death atomicity for both legacy and Rust. The complete structural
matrix still finishes with a successful Rust Gate self-test and legacy
rollback.

The Candidate profile is qualified at Worldline commit `e9ff4e55a` by
`Gate --executor-candidate`. It compares legacy and Rust Windows Jobs over a
real cold `Gate --candidate m702-powered-rail-slope-propagation` build in
independent disposable worktrees. Promotion requires byte-identical canonical
class evidence, complete output, zero remaining descendants, and a valid
host-bound lower-only admission receipt. This profile still does not make Rust
the default executor and does not start Minecraft.

### A6. Server-headless promotion

Run legacy and Rust executions sequentially against fresh official-server
workspaces under the same canonical local lease. Require:

- equal normalized behavioral observations;
- no remaining descendants;
- exact output ownership;
- no unplanned network access;
- fresh independent worlds;
- preserved proof and pin validation.

Status: the first Windows behavior/containment slice is qualified at Worldline
commit `7365d9a260` with Rust executor commit `803861d`. Legacy and Rust run
the canonical M20 official-server lifecycle sequentially under one continuous
lease in independent disposable worktrees. Their canonical evidence is equal,
both sides drain their process trees, and the Rust receipt proves complete
output plus zero descendants. Full A6 remains pending a broader server matrix
and an OS-enforced Windows network boundary; this slice observes the fixture's
explicit loopback binding under `network=inherit` and does not relabel that as
network isolation.

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

Status: the native Pbuffer slice is qualified at Worldline commit `9b3bad608`
by `Gate --executor-gui`. Legacy and Rust run the same official/mapped M10
oracle in independent exact-commit cells under one runtime lease. Both arms
prove four-process repetition, mapped/official frame equality, Pbuffer current
state, absent onscreen `Display`, correct renderer class origin, complete
output, and an empty process tree.

The pinned Aero slice is qualified at Worldline commit `935d0fc41` by
`Gate --executor-aero`. M778 builds exact Aero revision
`091940b8e0cbd839169d8eb977992dfee83faff6` inside independent private Gradle
homes, launches three graphical clients per arm, and proves exact framebuffer
equality plus lower culling work across two counterbalanced sessions. The
12-GiB/64-process profile peaks at 4,362,235,904 bytes and eleven processes
with complete output and an empty process tree. Broader onscreen profiles,
driver/GPU diagnostics, and coordinator-death GUI fixtures remain pending.

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

Status: qualified for the structural tooling profile at Worldline commit
`69797aede` and for the first server-headless differential at commit
`7365d9a260`. Legacy and Rust paths receive cleared, explicit environments
from the same Java policy. The server profile carries only named tool/runtime
controls and a delegated lease identity; its capability secret stays outside
the execution plan. Public descriptors omit all values and reject
secret-bearing names. Commit `e9ff4e55a` adds the Candidate allowlist with
private temporary/control roots, bounded build workers, and a bounded Gate
lease wait. Commit `9b3bad608` qualifies the first GUI allowlist with private
temporary/control roots and a delegated lease whose secret stays outside the
plan. Commit `935d0fc41` qualifies the Aero allowlist with private temporary,
control, and Gradle-home roots. Delegated runtime capabilities now bind PID
plus process start time and carry no secret values in the plan. Display and
GPU identity remain diagnostic rather than portable behavior inputs.

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

Status: the structural tooling profile has a qualified Java-owned declaration
and post-execution output invariant at Worldline commit `1ea3706ff`. Windows
Job Objects still provide no native filesystem sandbox, so this status does
not claim write prevention. Linux Landlock enforcement exists in shadow and
runtime-specific mount declarations remain pending.

The server-headless slice at commit `7365d9a260` strengthens output ownership
through fresh exact-commit worktrees, local-only official-artifact
materialization, safe no-follow cleanup, and worktree pruning. This proves
cell disposal and absence of residue, not prevention of writes while a
Windows Job is running.

The Candidate slice at commit `e9ff4e55a` applies the same exact-commit cell
lifecycle to static builds. Its evidence traversal is no-follow, accepts only
regular `.class` files under Candidate-owned output roots, and bounds both
file count and bytes. Each arm owns an independent cache and complete cleanup
leaves no worktree residue. Windows still provides no write-prevention claim.

The native GUI slice at commit `9b3bad608` copies only the bounded local
b1.7.3 workspace into each disposable exact-commit cell with no-follow
traversal, regular-file enforcement, and explicit file/byte ceilings. Input
hashes and mapped class presence are reverified by the M10 oracle. Cleanup
proves no cell residue, not Windows write prevention.

The Aero slice at commit `935d0fc41` materializes only two bounded official
artifacts plus an exact detached external checkout in each disposable cell.
The external worktree is removed and pruned before its containing Worldline
cell, and timeout paths terminate descendants from deepest child to parent and
confirm death. Independent private Gradle homes avoid ambient cache coupling;
Windows still provides no filesystem write-prevention claim.

The same profile now has qualified transactional publication at commit
`af7138e74`: staging and destination are siblings, publication is atomic and
no-replace, pre/post tree digests must agree, collisions preserve both trees,
and verification failure cannot publish success.

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

Status: qualified at Worldline commit `3acea88fc`. Every one of the 624 smoke
roots remained byte-identical during promotion, provider and train source pins
were migrated through their sealed tools, and the legacy algorithm is retained
only as a complete-catalog equality audit.

## 7. Workstream C: execution telemetry and adaptive admission

### C1. Structured telemetry

Collect peak resident memory, commit charge where available, process count,
CPU time, I/O, output volume, bootstrap duration, runtime duration, limit
events, and termination cause.

Status: phase telemetry is qualified for the structural tooling profile at
Rust executor commit `803861d` and Worldline commit `523166a81`. Metrics v2
measures one monotonic interval, partitions it at the platform run boundary,
allows only the one-millisecond integer-rounding difference, and keeps the
telemetry explicitly observational. Existing memory, process/task, CPU, I/O,
output, limit-event, and termination fields remain intact. Historical
aggregation and GUI profiles remain pending. The first official server profile
is qualified at Worldline commit `7365d9a260`: elapsed 15,906 ms, bootstrap
15 ms, runtime 15,891 ms, peak Job commit 809,144,320 bytes, peak seven
processes, complete output, and zero remaining descendants.

The first Candidate profile at commit `e9ff4e55a` records a successful real
M702 cold build with peak Job commit 1,368,981,504 bytes, canonical compiled
evidence, complete output, and zero remaining descendants.

The first native GUI profile at commit `9b3bad608` records M10 at 7,166 ms,
12-ms bootstrap, 7,153-ms runtime, 741,785,600-byte peak Job commit, seven
peak processes, complete output, and zero remaining descendants.

The first Aero GUI profile at commit `935d0fc41` records M778 at 787,205 ms,
15-ms bootstrap, 787,190-ms runtime, 4,362,235,904-byte peak Job commit,
eleven peak processes, complete output, and zero remaining descendants.

Native campaigns now retain already verified Rust receipt metrics as sanitized
`campaign-worker` samples. Nightly, structural instability, official worldgen,
and sealed M9 minimization replay samples remain observational and cannot
influence Candidate admission.

### C2. Historical profiles

Aggregate by lane, smoke ID, executor version, OS, architecture, and relevant
runtime capability. Measurements remain local or sanitized before upload.

Status: qualified for the structural Windows tooling profile at Worldline
commit `5d75d057c`. Local samples are immutable by content digest, strictly
bounded, canonically reparsed, and rebuild into deterministic profile groups.
Only sanitized observational fields are retained. Commit `7365d9a260` adds the
first smoke-specific official server sample for `m20-server-bootstrap` on
Windows 11/amd64. Commit `e9ff4e55a` adds the first Candidate sample for
`m702-powered-rail-slope-propagation` on Windows 11/amd64. Broader server and
multi-Candidate histories remain pending.
Commit `9b3bad608` adds the first native GUI sample for `m10-native-render` on
Windows 11/amd64; no history upload is enabled.
Commit `935d0fc41` adds the first Aero GUI sample for
`m778-aero-culling-visual-integrity` on Windows 11/amd64. Broader Aero,
onscreen, and cross-host histories remain pending; no history upload is enabled.
Commit `df7a35a9a` adds the first Nightly campaign history sample and exposes it
through the live Evidence Explorer without argv, paths, environment values, or
workload log content.
Commit `9dc03ddce` adds two passing structural-instability samples, while failed
inner runs remain native execution evidence rather than admissible history.
Commit `a3137efa7` adds the first passing official-worldgen campaign sample. Its
failed pre-oracle predecessor remains linked native evidence, not history.
Commit `c759a36d2` adds the first passing sealed-M9 minimization campaign sample.
Its two pre-oracle predecessors remain linked native evidence, not history;
with only one passing sample its performance classification is explicitly
`insufficient-history`.
Commit `924b97012` qualifies observational campaign performance signals over
that sanitized history. A reviewed three-sample baseline window is compared
with the immediately following three-sample recent window by median elapsed
time. The symmetric 150-percent band reports `increase`, `decrease`,
`within-band`, `zero-baseline`, or explicit `insufficient-history`; the schema
fixes both `verdict_authority=false` and `admission_effect=false`. Profiles bind
the campaign ID, budget, shard count/index, and resolved workload case counts,
plus the existing executor/backend/host dimensions. Seeds remain excluded so
replays are comparable, while a changed execution shape starts a new history.

Six sequential native structural-instability runs at seeds 201 through 206
qualify the complete live path for profile
`structural-instability-b4cb935723713dbb`. The first window median is 30,410 ms
and the recent window median is 28,770 ms, producing a 94-percent
`within-band` signal. All six runs are linked, with no failures, orphans,
partial evidence, truncation, or dangling campaign bindings; the aggregate
profile reports 174,401 ms elapsed, 669,097,984-byte peak Job commit and seven
peak processes. The six telemetry object digests begin with `9a2fc72d3f6c`,
`a98308919183`, `8d5b5c1c90f1`, `2966ceb12ade`, `82c40801c93c`, and
`ffe6f9dc7692`. The exact slice passes the canonical Gate at 135,250 ms.

Commit `f556e0f7b` adds a deterministic triage projection above these signals.
It produces investigation candidates only for `increase` and `zero-baseline`,
binds every case to the exact telemetry-object windows, and independently
cross-validates the case against its current signal in the live Explorer. It
does not schedule a replay or confer performance, behavior, or admission
authority.

### C3. Conservative admission

Feed observations into the existing adaptive learning barrier. Measurements
may lower admission immediately for safety. Increasing width still requires
the reviewed promotion sequence, repeated frozen waves, and unchanged
behavioral signatures.

Status: the lower-only policy and file contract are qualified at Worldline
commit `192061dc5`; commit `e9ff4e55a` exercises it with the first real
`candidate-worker` sample. The measured 1,368,981,504-byte peak plus the exact
25-percent margin produces a 1,711,226,880-byte host-bound floor. Because that
is below the configured 2-GiB worker estimate, effective admission stays at
2 GiB: telemetry cannot raise width. Increasing width still requires repeated
frozen waves and the reviewed promotion sequence.

### C4. Failure clustering

Group infrastructure failures by stable signatures without merging behavioral
divergences. OOM, timeout, escaped descendant, tool absence, provider failure,
and oracle mismatch remain distinct classes.

Status: qualified for sanitized structural telemetry and deterministic
read-model fixtures at Worldline commit `3947fa8e0`. Infrastructure,
provider, and behavioral domains are explicit, and provider failure cannot be
collapsed into oracle mismatch. The first successful official server sample
at commit `7365d9a260` rebuilds with zero failures and zero clusters. Real
failure histories plus provider samples remain pending. The first
successful Candidate sample at commit `e9ff4e55a` also rebuilds with zero
failures and zero clusters. The successful M10 GUI sample at commit
`9b3bad608` likewise adds no failure cluster.
The successful M778 Aero sample at commit `935d0fc41` also adds no failure
cluster.
Commit `85969eab4` adds the first qualified real native campaign failure
history. It validates the complete campaign-to-plan-to-receipt binding and
groups two minimization exits into one stable `unclassified-workload` cluster.
It deliberately does not reinterpret those exits as behavioral, provider, or
infrastructure failures. Executor containment failures remain separate classes,
and every cluster remains observational with no admission effect.

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

The local shared-directory backend is implemented for signed module bundles.
It is opt-in, preserves the common immutable object layout, admits only a
prepared publication plan, and independently revalidates every hydrated byte.
Its read-only inventory verifies signed references and classifies corrupt,
malformed, missing, and orphaned objects without leaking paths or content.
Per-digest local and cross-process locks serialize publication across Gates;
the inventory validates their separate zero-byte namespace without counting
locks as content objects.
S3-compatible storage and its TLS, authentication, retention, audit, and
publisher-governance policies remain pending rather than being inferred from
the shared filesystem boundary.

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

The first canonical slice is qualified. `Gate --evidence-explorer` validates
and projects every tracked smoke descriptor, qualification pin, proof envelope,
train provenance record, and supervised disposition into deterministic JSON
and dependency-free static HTML. The projection exposes proof class, pin
source, train class, hashes, last exact executed head when recorded, disposition
state, and orphan-envelope counts. It performs no writes to authoritative
evidence and explicitly declares `verdict_authority=false`.

The second slice adds a separate live overlay for validated telemetry profiles,
failure clusters, lower-only admission state, queue/lease delays, and all
current cache decisions. A third slice correlates native executor evidence with
campaign receipts and validates referenced, orphan, and missing campaign logs.
Those ephemeral inputs remain visually and structurally separated from the
canonical snapshot and cannot change its identity or confer PASS. Worker
attestations and non-campaign heavy-log families remain pending. Native links
also validate termination and exit identity, count nonzero or non-exited runs,
and expose their exit codes. This prevents a failed inner campaign coordinator
from appearing healthy merely because the outer Rust executor exited normally.
A closed campaign catalog now supplies every report root, so newly registered
adapters cannot silently disappear from the combined orphan analysis. A fourth
live slice derives content-bound regression investigation candidates from
campaign performance signals and cross-validates them before rendering. The
projection is rebuildable and cannot schedule work, write dispositions, affect
admission, or assert a verdict. A fifth live slice clusters only validated
campaign-linked native failures. It exposes containment failures as explicit
executor-infrastructure classes while preserving nonzero workload exits as
unclassified, content-bound observations rather than guessed verdicts.

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

The base contract, Nightly fuzz-plus-mutation adapter, structural instability
adapter, fixed official-worldgen matrix replay adapter, sealed M9 minimization
replay adapter, and the first observational profiling/regression read model are
qualified.
Plans and receipts are canonical, shards are complete and disjoint, immutable
publication is fail closed, and aggregation is independent of worker arrival
order. Native executor dispatch is qualified for the complete Nightly shard
coordinator on Windows, and its verified metrics now enter sanitized local
history without affecting admission. The structural instability adapter also
runs through Rust, supports multi-shard local arrival, and records verified
metrics under the same non-admissible capability; it deliberately does not
start Minecraft. The worldgen adapter runs the existing M634 oracle through the
official lease inside Rust containment, while leaving M634 as the sole behavior
authority. The minimization adapter similarly executes only the sealed M9
contract through its milestone Gate, leaving all reduction and behavior
authority in M9 while measuring the complete isolated replay. Campaign
profiling now has workload-shape-safe historical windows and live change
signals. Deterministic observational triage turns increases and invalid zero
baselines into content-bound investigation candidates, but still does not
schedule follow-up work or assert a regression verdict. Native campaign
failure clustering now deduplicates validated containment failures and
unclassified workload exits without merging them into behavioral divergences
or changing admission. Arbitrary-seed
worldgen, Minecraft-runtime instability adapters, differential execution,
arbitrary-input minimization queues, reviewed automatic follow-up execution,
plus multi-shard distribution across distinct hosts remain pending.

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
