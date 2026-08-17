# M9 Scenario Minimization Evidence Map

## Claim

Worldline can automatically reduce a noisy ordered scenario while repeatedly
reexecuting two real mod versions and preserving their exact first-divergence
predicate and artifact provenance.

## Executable evidence

The original nine steps are:

```text
observe:before, reseed:101, tap:2, tap:6, tick,
reseed:202, observe:target, tick, observe:after
```

For every uncached candidate, the evaluator loads the deterministic M8 v1.0.0
and v1.1.0 JARs through separate fresh classloaders, creates a fresh controlled
b1.7.3 runtime for each, interprets the remaining steps, and compares their
canonical traces.

The completed result contains exactly:

```text
observe:before
tick
observe:target
```

Removing `observe:before` shifts the divergence record from index 1 to 0;
removing `tick` removes the value delta; removing `observe:target` removes the
target record. Each therefore fails the full fingerprint. Two fresh outer JVMs
must agree on the 21 evaluations and byte-identical original/minimized files.

The frozen evidence SHA-256 is
`90add5dbac4599dfbb8556efd233a7b53371644200d60e9356605ca7854268b3`.
All derived scenarios, classes, and evidence remain under ignored `.worldline/`.

## Boundary

The evaluator uses the already oracle-qualified controlled client and the exact
M8 JAR hashes. The mod divergence remains an explicitly enabled modification;
the preceding client cycle owns vanilla equivalence. See
`docs/M9_MINIMIZATION.md` for algorithm guarantees and non-claims.
