# Worldline Engineering Guide

All repository artifacts must be written in English.

## Behavioral constitution

1. The official Minecraft Beta 1.7.3 JAR is the behavioral oracle.
2. Never assume decompiled source is semantically exact.
3. Every invasive runtime modification must preserve vanilla-observable
   behavior or be explicitly enabled in controlled-runtime mode.
4. Every controlled boundary must eventually have a differential or invariant
   test before it can be called implemented.
5. Never commit or distribute the official JAR, original assets, or decompiled
   Minecraft sources. Commit original Worldline code, mappings, patches, and
   transforms only.

## Engineering constitution

1. Each product source file must remain at or below 250 `tokei` code lines.
2. Each harness source file must remain at or below 300 `tokei` code lines.
3. Each smoke source file must remain at or below 150 `tokei` code lines.
4. Each adapter source file must remain at or below 150 `tokei` code lines.
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
