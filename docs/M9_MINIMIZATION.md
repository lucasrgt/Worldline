# M9 Automatic Scenario Minimization Contract

## Scope

M9 represents an ordered scenario independently of any game adapter and reduces
it by repeatedly asking a caller-supplied evaluator whether an exact predicate
still holds. The built-in divergence fingerprint preserves the complete M6
first-divergence report: kind, record index and label, field index and name, and
ordered left/right values.

The `worldline-minimization` module depends only on `worldline-analysis`.
Adapters interpret opaque steps and own execution isolation; the minimizer does
not import Minecraft, mod, replay, or runtime types.

## Canonical `.wlscenario` format

The artifact is strict UTF-8, LF-only, checksum-protected, at most 1 MiB, and
contains an ordered sequence between its header and checksum:

```text
WORLDLINE-SCENARIO/1
step=<unpadded URL-safe Base64 of one visible-ASCII step>
step=<next step>
sha256=<SHA-256 of every preceding line including LF>
```

Scenarios contain at most 4,096 steps. Each step is non-empty visible ASCII and
at most 1,024 UTF-8 bytes. Empty scenarios are canonical because a divergence
may require no actions. Parsing validates bounds, framing, Base64, UTF-8,
checksum, and exact byte-for-byte reconstruction.

The neutral CLI can author and inspect artifacts without executing them:

```text
worldline scenario create <output.wlscenario> [step ...]
worldline scenario inspect <scenario.wlscenario>
```

Creation never overwrites an existing path. Success exits 0, invalid input or
I/O failure exits 1, and usage errors exit 2.

## Minimization algorithm

`ScenarioMinimizer.minimize(original, maxEvaluations, evaluator)` first requires
the original scenario to satisfy the predicate. It performs deterministic
delta-debugging chunk removal, then restarts ordered single-step removal until
no individual step can be deleted. Scenario SHA-256 values cache evaluator
answers. The evaluator must therefore be deterministic for identical bytes.

The result records original/minimized artifacts, actual evaluator calls,
removed step count, and `complete`. `complete=true` proves one-minimality under
order-preserving deletion. If the positive evaluation budget is exhausted,
the best predicate-preserving candidate is returned with `complete=false`; it
must not be presented as minimal.

`DivergenceFingerprint.from(diff)` rejects equality and captures the entire
rendered first-divergence predicate. A candidate matches only if every captured
component remains identical.

## b1.7.3 evidence vocabulary

The M9 smoke's adapter interprets `tick`, `reseed:<long>`, `tap:<key>`, and
`observe:<label>`. These commands are evidence vocabulary, not universal
scenario opcodes. Every evaluator call opens fresh mod classloaders and fresh
controlled runtimes for both M8 versions.

## Non-claims

M9 proves one-minimal deletion, not the globally shortest or simplest scenario.
It does not shrink step values, reorder steps, synthesize new actions, infer
adapter vocabularies, tolerate nondeterministic evaluators, minimize private
heap/mod state, run untrusted code safely, or claim Aero/render compatibility.
