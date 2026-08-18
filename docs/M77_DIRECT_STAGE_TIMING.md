# M77 direct stage timing

Status: GO in Worldline v1.65.0.

M77 adds direct synchronous timers to the constant sixteen-block-entity M74
fixture. It records the complete Worldline block-entity renderer invocation,
the sixteen nested `Aero_BECellRenderer.queueAtRest` calls, and the two Aero
`flush` calls made in each retained renderer interval. The renderer and
`queueAtRest` spans are nested and must not be added together.

The common fixture block entity intentionally has no Aero dependency and does
not implement `Aero_CellRenderableBE`. Consequently all sixteen `queueAtRest`
calls take Aero's direct-render fallback and the two flush calls observe an
empty page queue. M77 times those exact method boundaries; it does not qualify
cell-page enqueue or populated-page flush cost.

The M77 overlay arms only after M74 fixture readiness. It validates vanilla
`fpsLimit=0`, disabled Aero frame pacing, and `present` mode, discards M74's
empty partial baseline if necessary, and starts both recorders at the following
renderer HEAD. Every stage record shares the exact index, count, elapsed time,
nonce, and concrete plan of one M74 complete-census record.

Hot-path state is preallocated and pre-touched. Each call uses `nanoTime` and
primitive accumulators; each frame copies primitive totals and call counts.
There is no retained per-sample allocation or I/O. After M74 seals, M77 writes
one versioned binary sidecar and the runner reparses it with the M74 artifact.

Both fresh qualified replicas observed exact call cardinality `16/16/2` in
every record. Renderer medians were `50100` and `48500 ns`; direct-fallback
`queueAtRest` medians were
`47700` and `46500 ns`; flush medians were `100 ns` in both. Individual flush
aggregates that fell below clock resolution remain zero rather than being
discarded or rewritten, while each complete flush series must be positive.

These are descriptive synchronous spans under M77's timing instrumentation.
They are not uninstrumented costs, independent samples, causal attribution,
regression/improvement evidence, statistical inference, pixel visibility,
cross-machine estimates, combat evidence, or historical lag reproduction.
