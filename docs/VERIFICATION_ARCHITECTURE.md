# Verification Architecture

This document is the operational architecture of Worldline verification. It is
derived from `Gate`, `RepositoryVerify`, the Runtime Fabric checks, and the
versioned receipt formats. Milestone narratives describe behavior; this file
describes how a claim becomes admissible release evidence.

## One entry point

`java tools/harness/Gate.java` is the only supported verification entry point.
It compiles the tracked harness by content digest, validates the requested
profile, acquires the required shared leases, and delegates to
`RepositoryVerify`. Calling a smoke coordinator directly is not qualification.

| Profile | Purpose | Official runtime |
| --- | --- | --- |
| default | Repository policy, products, adapters, and tests | Never |
| `--candidate ID` | Fast milestone-local static feedback | Never |
| `--milestone ID` | Clean static phase followed by isolated runtime proof | When the descriptor requires it |
| `--runtime` | Repository verification with required local oracle inputs | Input validation only |
| `--smoke` | Full smoke catalog, restoring verified receipts first | Cache misses only |
| `--pinned-smoke` | Prove that every requested observation is portable | Never executes a miss |
| `--orchestrator` | Qualify an exact clean integration-train SHA | Never substitutes for milestone runtime proof |

## Stage flow

The default repository flow is ordered and fail-closed:

```text
configuration
  -> harness self-tests and smoke discovery
  -> receipt/retry/test-cache/JSON/lease/pre-push self-tests
  -> release and optimization contracts
  -> behavior completeness and adapter kinds
  -> official artifact identity
  -> source policy and migration attestations
  -> Runtime Fabric self-test
  -> integration tools and orchestrator policy
  -> smoke-runner compilation
  -> module DAG compilation
  -> portable adapter contracts
  -> Atlas/TestKit milestone surfaces
  -> complete module test suite
  -> smoke suite, only for the smoke profile
```

`reports/verify.json` records the profile, every stage, duration, failure
message, and stack trace. A cached stage is a verified restoration of the same
stage result, not an omitted check.

## Behavioral identity and execution lanes

A smoke qualification fingerprint contains the descriptor inputs that affect
behavior, its runner and shared support, relevant product-module digests,
official artifact identities, and process configuration. Platform and lane
identity are recorded separately so a reviewed cross-lane differential can
eventually make portable observations reusable without pretending that two
uncompared environments are equivalent.

The runtime fingerprint excludes documentation-only qualification fields.
Changing a behavior input creates a cache miss. Adding a milestone executes
that milestone only. A reviewed source-only refactor may transport prior
observations only through a versioned migration lock that binds prior and
current fingerprints, exact source hashes, and unchanged evidence bytes.

## Receipts, pins, and evidence

The evidence chain has three distinct layers:

1. An execution produces a normalized observation and heavy local logs.
2. A schema-5 envelope under `smokes/qualification-evidence` binds the smoke
   ID, fingerprint, observation digest, provenance, and attestation bytes.
3. `smokes/qualification.lock` indexes the verified envelopes for portable
   clones. The lock row alone cannot manufacture a pass.

The local receipt cache is content-addressed and may restore a result only
after verifying its envelope. Pinned restoration verifies tracked evidence
bytes. Missing, stale, corrupt, wrong-platform, or unrecognized evidence fails
closed. Heavy logs may be garbage-collected; the small proof envelope and its
lock entry remain versioned.

## Leases and Runtime Fabric

Static verification uses a bounded shared slot pool. Official Minecraft work
uses a separate FIFO lease in the common control directory. Tickets are
published atomically and identify both PID and process start time, preventing
partial-write and PID-reuse ownership errors. Stale owners are pruned only
after liveness validation.

Milestone qualification has two phases. Static work runs in the shared slots;
the runtime phase waits for the official lease. Timeouts terminate descendants
in reverse order and confirm process death. Windows workers own complete trees
with Job Objects. Linux workers use cgroups v2 and may add namespace isolation.
The official client and server JARs remain ignored, local, hash-verified,
read-only oracle inputs.

## Runtime pools

The host pool is the low-overhead backend for isolated processes. Its admission
model accounts for current free memory, CPU units, process count, and per-lane
limits. The container pool adds read-only roots, private writable volumes,
dropped capabilities, and no external network. Both use one process boundary
per game, preserve per-task evidence, and share the same official-runtime
coordination contract.

Graphical Windows clients form a separate lane with larger resource budgets
and a smaller concurrency ceiling. Native GUI, OpenGL, Gradle mutation, and
RetroMCP mutation never inherit headless-server portability without their own
isolation and differential evidence.

## Shared caches

Module, test, runner, receipt, observation, and verification-stage caches are
immutable content-addressed families. Publication never replaces an existing
digest directory. Consumers read immutable entries directly or through safe
junctions. Usage markers and per-entry locks protect live worktrees while the
bounded maintenance policy enforces age and total-size limits.

Cache restoration always validates format and identity. A cache doctor reports
corruption without deleting evidence; garbage collection is a separate,
bounded operation. CI may restore the same caches because identity is derived
from tracked inputs rather than checkout location.

## Integration authority

Milestone workers stop at a clean committed SHA and hand off the worktree path,
ID, and commit. `IntegrationTrain` reconciles those commits onto an explicit
base and records conflicts. The orchestrator gate then binds authorization to
one clean train SHA. A candidate pass, dirty-tree run, direct smoke invocation,
or receipt from another input identity is never release evidence.

