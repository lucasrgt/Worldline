<!-- worldline-map-schema=1 -->
<!-- boundary=aero-frame-census -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=973ae93f8127bae80ceeddc372713f5968213aa1f2fb3a8978c58af61439ac40 -->

# M76 behavior map

M76 reuses the frozen M74 sixteen-cell synchronized fixture and binary census.
After exact readiness and before the first retained interval, a client-only
overlay selects one of three structural treatments:

- `no-dispatch`: remove the exact block-entity renderer registration (`0/0`);
- `dispatch-only`: retain sixteen renderer/probe calls and suppress Aero (`16/0`);
- `aero16`: retain sixteen renderer/probe calls and sixteen Aero calls (`16/16`).

The treatment property is client-only and read once. The server always places
the same sixteen exact identities. Every record must retain state `16/16`, mask
`0xffff`, visible chunks, and the treatment's exact renderer/Aero call pair.
Vanilla `fpsLimit=0` and disabled public Aero frame pacing are runtime-checked.

Two fresh triplets reverse order. Within each triplet, nonce, concrete plan,
fixed camera, synchronized content, heap, recorder, and window are equal. A
single retry is allowed only for a timeout after trigger and before census
start; it receives fresh JVMs/workspace. All later failures are terminal.

Frozen trace:

```text
v1|design=2-mirrored-fresh-triplets-noDispatch/dispatchOnly/aero16+aero16/dispatchOnly/noDispatch|fixture=constant16-synced+exact-plan-camera-nonce|treatments=renderer-map-removed-after-ready+renderer-body-noAero+renderer-body-aero16|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|baseline=setup-before-first-retained-head-to-head|window=min720intervals+12s|capture=M74-fixed-primitive+post-seal-binary|per-record=noDispatch:0/0+dispatchOnly:16/0+aero16:16/16|retry=one-pre-census-timeout-only|stats=descriptive-stage-deltas-dynamic|causality-attribution-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `973ae93f8127bae80ceeddc372713f5968213aa1f2fb3a8978c58af61439ac40`.

Nonclaims: stable ordering, isolated renderer or Aero cost, causal or inferential
effect, regression/improvement, significance, pixels, cross-machine generality,
combat relation, or historical lag reproduction.
