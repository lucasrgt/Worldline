# Worldline Vision

Worldline is a programmable laboratory for Minecraft Beta 1.7.3. It executes
the real game under external control, treats the official JAR as the behavioral
oracle, and virtualizes external boundaries only where an experiment requires
control.

The long-term loop is:

```text
freeze -> advance -> observe -> snapshot -> replay -> branch -> compare
```

The project values reproducible evidence over implementation authorship. It
does not promise absolute JVM, operating-system, GPU, or universal-mod
determinism. Every promoted claim must state its observable scope and survive a
differential or invariant test.

The first official result is deliberately small: Worldline v0.0.1 boots the
real b1.7.3 client headlessly, loads a world, advances one manual client tick,
and matches the official vanilla oracle.
