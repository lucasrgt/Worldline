<!-- worldline-map-schema=1 -->
<!-- boundary=aero-frame-census -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=92c9e4e28b17dd1df6750e5aff15022619211a1e981ffb9c3ccea461a3d9da05 -->

# M75 behavior map

M75 reuses the frozen M74 mod, synchronized sixteen-cell scene, fixed camera,
complete census, and binary schema. All arms place, transfer, reconcile, and
dispatch the same sixteen exact block entities. A client-only redirect is the
sole treatment: it forwards the first `N` nested nonce-ordered renderer calls to
the real `Aero_BECellRenderer.queueAtRest` boundary for `N = 0, 1, 4, 16`.

The selected sets are nested by the M74 identity index `dz * 4 + dy`. The server
does not receive the level property. Every artifact must retain structural
density sixteen, state `received=16/applied=16`, mask `0xffff`, sixteen renderer
dispatches per interval, and exactly `N` Aero renders and display-list calls per
interval. Thus levels vary Aero exposure while plan, network state, content,
camera, dispatch, recorder, heap, and window stay configured alike.

Two fresh ladders reverse order: `0/1/4/16` then `16/4/1/0`. Each ladder has one
nonce and one concrete plan reused across its four fresh server/client JVM pairs.
A single retry is allowed only for a client timeout after trigger and before
census start; it uses a fresh workspace/JVM. Measurement, parser, treatment,
artifact, and post-start failures are terminal.

Frozen trace:

```text
v1|design=2-mirrored-fresh-ladders-0/1/4/16+16/4/1/0|fixture=constant16-synced-dispatched+exact-plan-camera-nonce|treatment=nested-firstN-aero-queueAtRest-calls|census=complete-head-to-head-after-fixture-ready|window=min720intervals+12s|capture=fixed65536-primitive+post-seal-binary|per-record=dispatch16+aeroCalls-level|retry=one-pre-census-timeout-only|stats=descriptive-whole-census+level-minus-zero-dynamic|monotonicity-causality-regression-density-response-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `92c9e4e28b17dd1df6750e5aff15022619211a1e981ffb9c3ccea461a3d9da05`.

Nonclaims: monotonic or dose response, content-density effect, isolated Aero
cost, causal or inferential effect, regression/improvement, statistical
significance, independence of frame records, pixels, cross-machine generality,
combat relation, or historical lag reproduction.
