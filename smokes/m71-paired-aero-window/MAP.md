<!-- worldline-map-schema=1 -->
<!-- boundary=aero-paired-experiment -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0b26d07ed6b08195a067bf8730b43f49ec596dae274c74f335f8a44576cb1d2b -->

# M71 Paired Aero Window Evidence Map

| Boundary | Exact evidence |
| --- | --- |
| Design | Four fresh matched pairs run in balanced `C/E, E/C, C/E, E/C` order |
| Equivalence | Every arm uses the same official server bytes, seed, names, fixture, pinned Aero revision, heap, logger, and viewport configuration |
| Common anchor | Both arms broadcast exact Packet3 chat and the real client applies the exact message at handler TAIL |
| Control | No Packet18 request or Packet7 attack is sent; matching attacker Packet18 or victim Packet38 contaminates and fails the arm |
| Event | Packet18 is sent before Packet7; the observer applies attacker Packet18/1 before victim Packet38/2 before the first measured renderer completion |
| Warmup | At least 300 renderer TAIL completions and five monotonic seconds precede the anchor |
| Window | At least 480 renderer TAIL completions and eight monotonic seconds follow the common anchor |
| Logger | Threshold 25 ms, heartbeat 200 ms, asynchronous file writes, fixed heap; only FrameSpike, GC, and Pulse rows inside the stdout bracket are measured |
| Artifact | Selected stdout rows must reappear in order in the normally flushed post-exit file; WorldFlush rows are excluded |
| Summary | Per-arm selected-row median, nearest-rank p95, maximum, compile work, GC-bearing rows, and visibility range; each pair reports event-minus-control descriptive deltas |

Rows are threshold/GC/heartbeat-selected observations, not a complete frame
census and not independent experimental units. The experimental unit is the
fresh arm and the comparison unit is the matched pair. Numeric values and delta
directions are dynamic evidence and are excluded from the frozen signature.

M71 does not claim causality, spike attribution, a performance regression or
improvement, statistical significance, pixel visibility, uninstrumented
production timing, identical process state, or the historical Aero-content lag
mechanism. Packet18 is not an ACK and Packet38 does not identify an attacker.

Frozen expected signature SHA-256: `0b26d07ed6b08195a067bf8730b43f49ec596dae274c74f335f8a44576cb1d2b`
