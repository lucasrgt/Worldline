# M76 qualification cycle

`RendererDecompositionCycle` verifies the pinned Aero origin/revision and the
reused M74 server-safe closure, builds Aero in a disposable worktree, and runs
six fresh arms in two mirrored treatment triplets. M74 and M75 remain unchanged.

The first triplet runs `no-dispatch/dispatch-only/aero16`; the second runs the
reverse. Before launch the init script writes vanilla `fpsLimit:0` and passes
the exact Aero pacing property. The client reads the actual Minecraft option
and public Aero pacer state before publishing one treatment marker. Setup is
excluded from every retained interval.

The parser binds schema, nonce, plan, minimum window, aggregate duration, exact
file length, trailing EOF, positive intervals, visible chunks, synchronized
state, identity mask, and the exact per-record call pair for each treatment.
It reports whole-census median, p95, p99, maximum, and adjacent stage deltas as
dynamic descriptive output. Neither signs nor magnitudes are promotion gates.

One retry is permitted only for the known timeout shape after trigger and
before census start, using a fresh workspace/JVM. No measured, treatment,
parser, artifact, or post-start failure is retried. Partial arm ranges require
an explicit diagnostic flag and cannot emit qualification evidence.

The frozen semantic trace reproduces SHA-256
`973ae93f8127bae80ceeddc372713f5968213aa1f2fb3a8978c58af61439ac40`.
