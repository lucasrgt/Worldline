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

1. Maintained product code must remain at or below 1,150 `tokei` code lines,
   and each product source file must remain at or below 250 code lines.
2. The verification, replay, and smoke tooling must remain at or below 2,250 `tokei` code lines,
   and each harness source file must remain at or below 300 code lines.
3. Executable smoke scenarios and oracle adapters must remain at or below 1,150
   `tokei` code lines, and each smoke source file at or below 150 code lines.
4. Game-specific adapters must remain at or below 1,200 `tokei` code lines, and
   each adapter source file at or below 150 code lines.
5. Tests are unlimited. Product behavior may not be moved into tests, generated
   files, or harness code to evade a budget.
6. Modules follow the dependency order declared in `harness.properties`.
   A module may depend only on modules explicitly listed there; cycles are
   forbidden and the harness compiles modules separately to enforce this.
7. Prefer the smallest complete implementation. Add an abstraction or
   dependency only when it creates a real boundary or removes more maintained
   behavior than it introduces.
8. Missing tools, missing tests, illegal dependencies, and unresolved checks
   fail closed.

The initial budgets are intentionally small. A deliberate milestone may revise
them in the same reviewed change, but silent growth is forbidden.

## Canonical verification

Before reporting implementation work complete, run from the repository root:

```text
java tools/harness/Verify.java
```

This is the canonical local and CI gate. It owns the source budgets, module
dependency enforcement, compilation with warnings as errors, and the complete
test suite. Do not substitute partial commands for it.

Any task that reads, transforms, instruments, or executes Minecraft must use
the runtime profile and may not proceed unless it passes:

```text
java tools/harness/Verify.java --runtime
```

The first deterministic vanilla smoke is the stronger executable gate:

```text
java tools/harness/Verify.java --smoke
```
