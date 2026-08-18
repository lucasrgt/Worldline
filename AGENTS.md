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
2. Each verification, replay, or smoke-tooling source file must remain at or
   below 300 code lines.
3. Each executable smoke scenario or oracle source file must remain at or
   below 150 code lines.
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

There is deliberately no repository-wide line cap. Growth must happen through
small cohesive files and explicit modules rather than oversized source files.
A deliberate milestone may revise a per-file limit in the same reviewed
change, but silent growth is forbidden.

## Canonical verification

Before reporting implementation work complete, run from the repository root:

```text
java tools/harness/Verify.java
```

This is the canonical local and CI gate. It owns the per-file source ceilings, module
dependency enforcement, compilation with warnings as errors, and the complete
test suite. Do not substitute partial commands for it.

A versioned pre-push hook runs the same gate before every push. Activate it
once per clone with `git config core.hooksPath tools/hooks`; export
`WORLDLINE_PREPUSH_SMOKE=1` to demand the full smoke suite instead.

Any task that reads, transforms, instruments, or executes Minecraft must use
the runtime profile and may not proceed unless it passes:

```text
java tools/harness/Verify.java --runtime
```

The first deterministic vanilla smoke is the stronger executable gate:

```text
java tools/harness/Verify.java --smoke
```

<!-- csm:instructions:start -->
## Codebase Semantic Memory

This repository uses CSM. Run `csm context --task "<goal>" --path <path>` before changing code and `csm check --task "<goal>" --base HEAD` before finishing. Durable tool state lives under `.csm/`; use `csm sync` to install the versions pinned by CSM. The standalone tools remain authoritative for their own records and must be invoked through `csm nya|rtw|wtw|nwc ...` in this repository.
<!-- csm:instructions:end -->
