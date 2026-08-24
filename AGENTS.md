# Worldline Engineering Guide

All repository artifacts must be written in English.

## Behavioral constitution

1. The official Minecraft Beta 1.7.3 client and dedicated-server JARs are the
   behavioral oracles.
2. Never assume decompiled source is semantically exact.
3. Every invasive runtime modification must preserve vanilla-observable
   behavior or be explicitly enabled in controlled-runtime mode.
4. Every controlled boundary must eventually have a differential or invariant
   test before it can be called implemented.
5. Never commit or distribute official JARs, original assets, or decompiled
   Minecraft sources. Commit original Worldline code, mappings, patches, and
   transforms only.

## Engineering constitution

1. Each maintained product source file must remain at or below 250 `tokei`
   code lines.
2. Each verification or replay source file must remain at or below 300 `tokei`
   code lines. Smoke-tooling files are limited to 300 executable statements.
3. Each executable smoke scenario or oracle source file is limited to 150
   executable statements. Reviewed legacy overages live in
   `quality/smoke-statement-debt.properties`; no new overage is permitted and
   every recorded allowance may only decrease.
4. Each game-specific adapter source file must remain at or below 150 code
   lines.
5. There is no total line budget. Tests are unlimited. Product behavior may
   not be moved into tests, generated files, or harness code to evade a
   per-file ceiling.
6. Modules follow the dependency order declared in `harness.properties`.
   A module may depend only on modules explicitly listed there; cycles are
   forbidden and the harness compiles modules separately to enforce this.
7. Prefer the smallest complete implementation. Add an abstraction or
   dependency only when it creates a real boundary or removes more maintained
   behavior than it introduces.
8. Missing tools, missing tests, illegal dependencies, and unresolved checks
   fail closed.
9. Maintained Worldline performance changes must have a stable optimization
   ID. Worldline-owned sites may use source-only `OptimizationRef`. External
   projects own their optimization records; Worldline may reference their IDs
   in evidence but must not copy project-specific implementation catalogs.
10. Runtime drivers are a closed set. Mods adapt to Worldline through
    `worldline/extensions/` manifests in their own repositories. Do not add
    an in-tree adapter per mod. Worldline may pin one overlay extension for
    oracled smokes. StationAPI is a future driver, not an extension.
11. Every new milestone must publish a stable `WorldlineBehavior` contract or
    an explicitly reviewed structural capability. Behavioral milestones must
    declare a reusable fixture, actions, observations, public binding, and
    equatable evidence in `smoke.properties`. Milestone numbers are progress
    aliases only and may not become the public TestKit identity.

There is deliberately no repository-wide line cap. Growth must happen through
small cohesive files and explicit modules rather than oversized source files.
A deliberate milestone may revise a per-file limit in the same reviewed
change, but silent growth is forbidden.

## Canonical verification

Before reporting implementation work complete, run from the repository root:

```text
java tools/harness/Gate.java
```

This is the canonical local and CI gate. It owns the per-file source ceilings, module
dependency enforcement, compilation with warnings as errors, and the complete
test suite. Removed `Verify.java` entry points must not be referenced. Do not
substitute partial commands for the canonical gate.

A versioned pre-push hook runs the same gate before every push. Activate it
once per clone with `git config core.hooksPath tools/hooks`; export
`WORLDLINE_PREPUSH_SMOKE=1` to demand the full smoke suite instead.

Any task that reads, transforms, instruments, or executes Minecraft must use
the runtime profile and may not proceed unless it passes:

```text
java tools/harness/Gate.java --runtime
```

The first deterministic vanilla smoke is the stronger executable gate:

```text
java tools/harness/Gate.java --smoke
```

The smoke gate persists one content-addressed PASS proof after every scenario and reuses proofs
whose behavior-input fingerprints are unchanged. It writes an aggregate receipt for the exact
current tree. Reviewed approvals may be promoted into the portable
`smokes/qualification.lock` only with `Gate.java --pin-smokes`. Use
`WORLDLINE_SMOKE_CACHE=off` only when a deliberate full rerun is required.

<!-- csm:instructions:start -->
## Codebase Semantic Memory

This repository uses CSM. Run `csm context --task "<goal>" --path <path>` before changing code and `csm check --task "<goal>" --base HEAD` before finishing. Durable tool state lives under `.csm/`; use `csm sync` to install the versions pinned by CSM. The standalone tools remain authoritative for their own records and must be invoked through `csm nya|rtw|wtw|nwc ...` in this repository.
<!-- csm:instructions:end -->

## Concurrent milestone work

1. Use one clean worktree and one branch per milestone. Never share a worktree
   between agents.
2. During implementation, run `java tools/harness/Gate.java --candidate ID`.
   Candidate verification must not start an official runtime.
3. Before handoff, commit a clean worktree and run
   `java tools/harness/Gate.java --milestone ID`. This isolated final gate must
   prove the frozen cycle, milestone and cycle documentation, semantic map,
   applicable mappings, and matching behavior Atlas/TestKit surfaces.
4. The milestone gate runs static work in shared parallel slots, then waits for
   the cross-platform official-runtime lease. Never bypass or delete lock files.
5. Repository verification is bounded by shared machine slots.
6. Milestone workers own their milestone directory and narrowly scoped product
   or adapter files. Global release indexes and generated catalogs are
   integration-train outputs, not worker-owned files.
7. Qualification receipts are bound to an exact clean commit and base. A
   candidate pass, dirty-tree run, or stale receipt is not release evidence.
8. Milestone workers stop after local qualification and hand off the worktree
   path plus commit SHA. They do not push milestone branches or merge `main`.
9. Do not invoke `tools/smoke/*.java` directly. Use `Gate.java --milestone ID` so
   timeouts, logs, process cleanup, and the runtime lease remain enforced.
10. The orchestrator qualifies a train with `java
   tools/integration/IntegrationTrain.java --base SHA ID=REF...`. Use
   `--plan-only` solely for a non-qualifying conflict audit.
   After reconciliation, it runs `java tools/harness/Gate.java --orchestrator`.
   Only that exact authorized SHA may be pushed. Audit worktrees with
   `java tools/integration/WorktreeLifecycle.java audit --base REF`.

The complete coordination contract is in `docs/ENGINEERING_WORKFLOW.md`.
