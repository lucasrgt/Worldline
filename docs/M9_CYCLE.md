# M9 Automatic Scenario Minimization GO Audit

Status: GO.

## Promotion evidence

| Requirement | Evidence | State |
| --- | --- | --- |
| Durable scenario | `.wlscenario` round-trips canonically with checksum and strict bounds | PASS |
| Exact predicate | Full M6 first-divergence rendering is captured and matched | PASS |
| Automatic reduction | Delta debugging plus ordered single removal executes without a hand-selected subset | PASS |
| Honest budget | Exhaustion returns the best preserved candidate with `complete=false` | PASS |
| Real reexecution | Every candidate opens both M8 mod versions in fresh classloaders and runtimes | PASS |
| Determinism | Two fresh minimization JVMs emit identical output and artifacts | PASS |
| Reduction | Nine steps reduce to three: `observe:before`, `tick`, `observe:target` | PASS |
| One-minimality | Removing each of the three final steps independently breaks the exact predicate | PASS |
| Provenance | Both M8 artifact hashes, original/minimized scenario hashes, and predicate hash are frozen | PASS |
| Corruption rejection | Modified minimized scenario fails neutral CLI inspection | PASS |
| Vanilla baseline | Preceding controlled-client cycle retains the direct official-JAR match | PASS |

The exact predicate is a value divergence at record index 1, label `target`,
field index 1 `block65`, ordered `20 -> 0`. Preserving record index explains why
the preceding `observe:before` step is causal to this exact predicate.

The frozen M9 evidence-report SHA-256 is:

```text
90add5dbac4599dfbb8556efd233a7b53371644200d60e9356605ca7854268b3
```

The minimizer used 21 evaluator calls. The smoke additionally reexecutes the
final result and every one-step deletion independently of the algorithm's own
cache before declaring the result one-minimal.

## Qualification

The canonical `java tools/harness/Verify.java --smoke` gate passed from both
the prepared workspace and a cold client reconstruction. The cold run invoked
RetroMCP decompile/recompile, rebuilt the adapter, deterministic M8 inputs, and
all M9 evaluations while preserving the frozen report. Rebuilt mapped
`Minecraft.class` retained SHA-256
`f3aba176750d89e28559b9c85b070d1819ed310b83a6703df002e768ad8ee14a`.
The redundant pre-reconstruction directory was sent to the Windows Recycle Bin.
