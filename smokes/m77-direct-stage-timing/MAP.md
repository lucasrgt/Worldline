# M77 behavior map

M77 reuses the frozen M74 synchronized sixteen-cell scene and complete census.
After exact fixture readiness, a client-only overlay validates max vanilla
framerate and disabled Aero pacing, discards only an empty partial baseline,
then records three synchronous boundaries with primitive `nanoTime` totals:

- complete `WorldlineCensusRenderer.render` aggregate: exactly 16 calls;
- nested `Aero_BECellRenderer.queueAtRest` aggregate: exactly 16 calls;
- `Aero_BECellRenderer.flush` aggregate: exactly 2 calls.

Each sidecar record is index-aligned with one M74 record and binds the same
nonce, plan, count, and elapsed duration. M74 simultaneously proves exact
state `16/16`, mask `0xffff`, sixteen renderer identities, sixteen real Aero
calls, and positive visible chunks. Both artifacts are serialized post-seal.

Zero flush nanoseconds are preserved when both calls fit inside clock
resolution; the whole series must still contain positive time. Renderer and
queue spans are nested, so their values are reported separately rather than
summed. Two fresh replicas use the same plan and nonce.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-replicas|fixture=constant16-synced-aero+exact-camera|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|timers=renderer+queueAtRest+flush-synchronous-nanoTime-primitive|window=M74-min720intervals+12s|capture=no-retained-allocation-io+post-seal-sidecar|per-record=renderer16+queue16+flush2+state16/maskffff|clock=zero-flush-spans-preserved+series-positive|stats=descriptive-direct-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `4ac829480cfb8a9409d89c35e002246e43a0a143815303e1ac520e8990988a4c`.

Nonclaims: uninstrumented or isolated cost, additive renderer-plus-queue time,
causality, regression/improvement, inference, pixels, cross-machine generality,
combat relation, or historical lag reproduction.
